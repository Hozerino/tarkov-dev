import com.sun.org.apache.xpath.internal.operations.Bool

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class MapMetadata(
    val name: String,
    val imageFile: String,
    val coordinateRotation: Int,
    val bounds: Pair<Pair<Double, Double>, Pair<Double, Double>> // (x1,z1), (x2,z2)
)

object MapRegistry {
    private val jsonString = this::class.java.getResourceAsStream("maps.json")?.bufferedReader()?.readText()!!
    private val jsonData = Json.decodeFromString<Map<String, JsonMapData>>(jsonString)

    val maps = jsonData.mapValues { (_, data) ->
        println("Registering map ${data.name}")
        MapMetadata(
            name = data.name,
            imageFile = data.imagefile.replace(".svg", ".png"), // Convert SVG to PNG
            coordinateRotation = data.coordinateRotation,
            bounds = (data.bounds[0][0] to data.bounds[0][1]) to (data.bounds[1][0] to data.bounds[1][1])
        )
    }

    val defaultMap = maps["customs"] ?: error("Default map 'customs' not found in registry")

    @Serializable
    private data class JsonMapData(
        val name: String,
        val imagefile: String,
        val bounds: List<List<Double>>,
        val coordinateRotation: Int
    )
}