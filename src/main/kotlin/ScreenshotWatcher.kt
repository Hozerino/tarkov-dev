import java.io.File
import java.nio.file.*
import java.util.regex.Pattern
import kotlin.concurrent.thread

object ScreenshotWatcher {
    private val watchDir = Paths.get("C:/Users/Hozer/Documents/Escape from Tarkov/Screenshots")
    private val coordPattern = Pattern.compile("_(-?\\d+\\.?\\d*),\\s*(-?\\d+\\.?\\d*),\\s*(-?\\d+\\.?\\d*)_")

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
        val matcher = coordPattern.matcher(filename)
        if (matcher.find()) {
            val x = matcher.group(1).toDouble()
            val y = matcher.group(2).toDouble()
            val z = matcher.group(3).toDouble()

            println("ScreenshotWatcher: Plotting coordinates from $filename")
            UIHandler.plot(x, y, z)
        } else {
            println("ScreenshotWatcher: Could not parse coordinates from $filename")
        }
    }

    fun stop() {
        watcherThread?.interrupt()
        watcherThread = null
    }
}
