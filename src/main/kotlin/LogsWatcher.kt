import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.io.path.toPath
import kotlin.text.RegexOption.IGNORE_CASE

object LogsWatcher {
    private const val LOCATION_SUBSTRING = "application|TRACE-NetworkGameCreate profileStatus"
    private const val LOCATION_RE = """location:\s*(?<loc>\S+),"""
    private const val LOCATION_SUBSTRING2 = "application|scene preset"
    private const val LOCATION_RE2 = """path:maps\/(?<loc>\w+)\.bundle"""
    private const val NOTIFICATION_SUBSTRING = "push-notifications|Got notification | ChatMessageReceived"
    private val LINE_START_WITH_DATE = Regex("^\\d{4}-\\d{2}-\\d{2} \\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{3}", IGNORE_CASE)

    private var initialLogsReadCount = 0
    private val isAllInitialLogsRead get() = initialLogsReadCount >= 2

    private val filePositions = ConcurrentHashMap<File, Long>()

    fun start() {
        val logFolder = File("D:\\Games\\Battlestate Games - Tarkov\\Escape from Tarkov\\Logs")
        if (!logFolder.exists() || !logFolder.isDirectory) {
            println("Watcher: logs folder not found: '${logFolder.absolutePath}'")
            return
        }

        val latestLogDir = getLatestLogFolder(logFolder) ?: return
        println("Watcher: monitoring logs folder: '${latestLogDir.absolutePath}'")

        listOf("application.log", "notifications.log").forEach { filename ->
            val file = File(latestLogDir, filename)
            if (file.exists()) {
                filePositions[file] = 0L
                Thread {
                    tailFile(file)
                }.start()
            }
        }
    }

    private fun getLatestLogFolder(logsRoot: File): File? {
        return logsRoot.listFiles { file -> file.isDirectory }
            ?.maxByOrNull { it.lastModified() }
    }

    private fun tailFile(file: File) {
        while (true) {
            try {
                val lastPos = filePositions[file] ?: 0L
                val raf = RandomAccessFile(file, "r")
                raf.seek(lastPos)

                var line = raf.readLine()
                while (line != null) {
                    if (!isAllInitialLogsRead) {
                        line = raf.readLine()
                        continue
                    }

                    when {
                        LOCATION_SUBSTRING in line -> {
                            parseLoc(line, LOCATION_RE)?.let { UIHandler.setMap(it) }
                        }
                        LOCATION_SUBSTRING2 in line -> {
                            parseLoc(line, LOCATION_RE2)?.let { UIHandler.setMap(it) }
                        }
                        NOTIFICATION_SUBSTRING in line -> {
                            val jsonBuilder = StringBuilder()
                            line = raf.readLine() // next line

                            while (line != null && !LINE_START_WITH_DATE.containsMatchIn(line)) {
                                jsonBuilder.appendLine(line)
                                line = raf.readLine()
                            }

                            val json = jsonBuilder.toString()
                            if (json.isNotBlank()) {
                                try {
                                    val element = Json.parseToJsonElement(json).jsonObject
                                    val msg = element["message"]?.jsonObject
                                    val type = msg?.get("type")?.jsonPrimitive?.content
                                    val templateId = msg?.get("templateId")?.jsonPrimitive?.content

                                    if (!templateId.isNullOrBlank() && !type.isNullOrBlank()) {
                                        val questId = templateId.split(" ").firstOrNull()
                                        if (!questId.isNullOrBlank()) {
                                            println("Watcher: questId: $questId, status: $type")
//                                            UIHandler.sendQuestUpdate(questId, type)
                                        }
                                    }
                                } catch (e: Exception) {
                                    println("Watcher: JSON error: ${e.message}")
                                }
                            }
                        }
                    }

                    line = raf.readLine()
                }

                filePositions[file] = raf.filePointer
                raf.close()

                if (!isAllInitialLogsRead) {
                    initialLogsReadCount++
                }

                Thread.sleep(500)
            } catch (e: Exception) {
                println("Watcher: error reading file ${file.name}: ${e.message}")
            }
        }
    }

    private fun parseLoc(line: String, regex: String): String? {
        return Regex(regex, IGNORE_CASE)
            .find(line)
            ?.groups?.get("loc")
            ?.value
            ?.lowercase()
    }
}
