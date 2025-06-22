import java.nio.file.*
import java.util.regex.Pattern
import kotlin.concurrent.thread

object ScreenshotWatcher {
    private val watchDir = Paths.get("C:/Users/Hozer/Documents/Escape from Tarkov/Screenshots")

    // Regex to extract position and quaternion from filename:
    // Example filename format:
    // 2025-06-20[00-04]_110.40, 22.80, -279.68_0.10187, 0.47786, -0.05913, 0.87050_12.52 (0).png
    private val filenamePattern = Pattern.compile(
        """_(-?\d+\.?\d*),\s*(-?\d+\.?\d*),\s*(-?\d+\.?\d*)_""" +                 // x,y,z position
                """(-?\d+\.?\d*),\s*(-?\d+\.?\d*),\s*(-?\d+\.?\d*),\s*(-?\d+\.?\d*)_""" +  // qx, qy, qz, qw quaternion
                """.*\.png""",
        Pattern.CASE_INSENSITIVE
    )

    private var watcherThread: Thread? = null

    fun start() {
        if (!watchDir.toFile().exists()) {
            println("ScreenshotWatcher: Folder does not exist: $watchDir")
            return
        }

        watcherThread = thread(start = true, isDaemon = true) {
            try {
                val watchService = FileSystems.getDefault().newWatchService()
                watchDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE)
                println("ScreenshotWatcher: Watching $watchDir")

                while (true) {
                    val key = watchService.take()
                    for (event in key.pollEvents()) {
                        val kind = event.kind()
                        if (kind == StandardWatchEventKinds.OVERFLOW) continue

                        val fileName = event.context() as Path
                        val fullPath = watchDir.resolve(fileName)

                        if (fileName.toString().endsWith(".png", ignoreCase = true)) {
                            processFile(fullPath.fileName.toString())
                        }
                    }
                    key.reset()
                }
            } catch (e: Exception) {
                println("ScreenshotWatcher: Error: ${e.message}")
            }
        }
    }

    private fun processFile(filename: String) {
        val matcher = filenamePattern.matcher(filename)
        if (matcher.find()) {
            val x = matcher.group(1).toDouble()
            val y = matcher.group(2).toDouble()
            val z = matcher.group(3).toDouble()

            // ORIGINAL QX QY QZ QW
            val qx = matcher.group(4).toDouble()
            val qy = matcher.group(5).toDouble()
            val qz = matcher.group(6).toDouble()
            val qw = matcher.group(7).toDouble()


            println("ScreenshotWatcher: Plotting position ($x, $y, $z) and quaternion ($qx, $qy, $qz, $qw) from $filename")
            UIHandler.plot(x, y, z, qx, qy, qz, qw)
        } else {
            println("ScreenshotWatcher: Could not parse coordinates/quaternion from $filename")
        }
    }

    fun stop() {
        watcherThread?.interrupt()
        watcherThread = null
    }
}
