import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object UIHandler {
    // --- Old code ---
    // var position by mutableStateOf(Triple(0.0, 0.0, 0.0))
    //     private set

    // fun plot(x: Double, y: Double, z: Double) {
    //     println("🧭 Plotting position: x=$x, y=$y, z=$z")
    //     position = Triple(x, y, z)
    // }

    // --- New image-based plotting ---
    var currentMap: MapMetadata = MapRegistry.defaultMap
        private set

    var relativePosition: Pair<Float, Float>? by mutableStateOf(null)

    fun setMap(name: String) {
        MapRegistry.maps[name.lowercase()]?.let {
            currentMap = it
        }
    }

    fun plot(x: Double, y: Double, z: Double) {
        println("Plot called with x=$x, z=$z")
        println("Map bounds: ${currentMap.bounds}")

        val (rotX, rotZ) = rotateCoordinates(x, z, currentMap.coordinateRotation)
        println("Rotated coords: x=$rotX, z=$rotZ")

        relativePosition = mapPositionToImage(rotX, rotZ, currentMap)
        println("Relative position on image: $relativePosition")
    }


    private fun mapPositionToImage(x: Double, z: Double, map: MapMetadata): Pair<Float, Float> {
        // Rotate world coords by -coordinateRotation degrees
        val rotation = -map.coordinateRotation
        val (rotX, rotZ) = rotateCoordinates(x, z, rotation)

        // Rotate both bounds corners by same rotation
        val (x1, z1) = rotateCoordinates(map.bounds.first.first, map.bounds.first.second, rotation)
        val (x2, z2) = rotateCoordinates(map.bounds.second.first, map.bounds.second.second, rotation)

        val minX = minOf(x1, x2)
        val maxX = maxOf(x1, x2)
        val minZ = minOf(z1, z2)
        val maxZ = maxOf(z1, z2)

        val relativeX = ((rotX - minX) / (maxX - minX)).toFloat()
        val relativeY = (1f - ((rotZ - minZ) / (maxZ - minZ))).toFloat() // top-down image axis

        return relativeX to relativeY
    }

    private fun rotateCoordinates(x: Double, z: Double, degrees: Int): Pair<Double, Double> {
        val radians = Math.toRadians(degrees.toDouble())
        val cos = kotlin.math.cos(radians)
        val sin = kotlin.math.sin(radians)

        val rotX = x * cos - z * sin
        val rotZ = x * sin + z * cos

        return rotX to rotZ
    }

}
