import com.sun.org.apache.xpath.internal.operations.Bool

data class MapMetadata(
    val name: String,
    val imageFile: String,
    val coordinateRotation: Int,
    val bounds: Pair<Pair<Double, Double>, Pair<Double, Double>> // (x1,z1), (x2,z2)
)

object MapRegistry {
    val maps = mapOf(
        "customs" to MapMetadata(
            name = "Customs",
            imageFile = "Customs.png", // make sure this file exists in resources/images/
            bounds = (698.0 to -307.0) to (-371.0 to 237.0),
            coordinateRotation = 180
        )
        // Add more maps here
    )

    val defaultMap = maps["customs"]!! // Hardcoded for now
}
