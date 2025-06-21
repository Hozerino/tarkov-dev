//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.jsonObject
//import kotlinx.serialization.json.jsonPrimitive
//import org.java_websocket.client.WebSocketClient
//import org.java_websocket.handshake.ServerHandshake
//import java.net.URI
//import java.util.regex.Pattern
//
//class TarkovWebSocketHandler(
//    private val onPlot: (x: Double, y: Double, z: Double) -> Unit,
//    private val onMapChange: (mapName: String) -> Unit = {}
//) : WebSocketClient(URI("ws://localhost:12345")) {
//
//    override fun onOpen(handshakedata: ServerHandshake?) {
//        println("✅ Connected to TarkovPilot WebSocket")
//    }
//
//    override fun onMessage(message: String) {
//        try {
//            if (message.trim().startsWith("SendFilename:")) {
//                val filename = message.substringAfter("SendFilename:").trim()
//                processFilename(filename)
//            } else if (message.trim().startsWith("{")) {
//                val json = Json.parseToJsonElement(message).jsonObject
//                when (json["messageType"]?.jsonPrimitive?.content) {
//                    "SEND_FILENAME" -> {
//                        val filename = json["filename"]?.jsonPrimitive?.content ?: return
//                        processFilename(filename)
//                    }
//                    "MAP_CHANGE" -> {
//                        val mapName = json["mapName"]?.jsonPrimitive?.content ?: "Unknown"
//                        println("🗺️ Map changed to: $mapName")
//                        onMapChange(mapName)
//                    }
//                    else -> {
//                        println("⚠️ Unhandled messageType: ${json["messageType"]}")
//                    }
//                }
//            } else {
//                println("⚠️ Unknown message format: $message")
//            }
//        } catch (e: Exception) {
//            println("❌ Failed to process message: $message\n$e")
//        }
//    }
//
//    override fun onClose(code: Int, reason: String?, remote: Boolean) {
//        println("❎ WebSocket closed: $reason")
//    }
//
//    override fun onError(ex: Exception?) {
//        println("❌ WebSocket error: ${ex?.message}")
//    }
//
//    private fun processFilename(filename: String) {
//        val pattern = Pattern.compile("_(-?\\d+\\.?\\d*),\\s*(-?\\d+\\.?\\d*),\\s*(-?\\d+\\.?\\d*)_")
//        val matcher = pattern.matcher(filename)
//        if (matcher.find()) {
//            val x = matcher.group(1).toDouble()
//            val y = matcher.group(2).toDouble()
//            val z = matcher.group(3).toDouble()
//            println("📍 Position received: x=$x, y=$y, z=$z")
//            onPlot(x, y, z)
//        } else {
//            println("❓ Could not extract coords from filename: $filename")
//        }
//    }
//}
