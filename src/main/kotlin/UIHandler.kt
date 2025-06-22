import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.*

object UIHandler {

    var currentMap: MapMetadata = MapRegistry.defaultMap
        private set

    var relativePosition: Pair<Float, Float>? by mutableStateOf(null)
    var lookQuaternion: Quadruple<Double, Double, Double, Double>? by mutableStateOf(null) // (qx,qy,qz,qw)

    fun setMap(name: String) {
        MapRegistry.maps[name.lowercase()]?.let {
            currentMap = it
        }
    }

    fun plot(x: Double, y: Double, z: Double, qx: Double, qy: Double, qz: Double, qw: Double) {
        // ... existing position rotation and mapping logic

        val (rotX, rotZ) = rotateCoordinates(x, z, currentMap.coordinateRotation)
        relativePosition = mapPositionToImage(rotX, rotZ, currentMap)

        // Store quaternion for look direction arrow
        lookQuaternion = Quadruple(qx, qy, qz, qw)
    }

    // Helper data class for 4 values (since Kotlin doesn't have Quadruple by default)
    data class Quadruple<A,B,C,D>(val x: A, val y: B, val z: C, val w: D)

    private fun mapPositionToImage(x: Double, z: Double, map: MapMetadata): Pair<Float, Float> {
        // Rotate world coords by -coordinateRotation degrees
        val rotation = map.coordinateRotation

        // Rotate both bounds corners by same rotation
        val (x1, z1) = rotateCoordinates(map.bounds.first.first, map.bounds.first.second, rotation)
        val (x2, z2) = rotateCoordinates(map.bounds.second.first, map.bounds.second.second, rotation)

        val minX = minOf(x1, x2)
        val maxX = maxOf(x1, x2)
        val minZ = minOf(z1, z2)
        val maxZ = maxOf(z1, z2)

        val relativeX = ((x - minX) / (maxX - minX)).toFloat()
        val relativeY = (1f - ((z - minZ) / (maxZ - minZ))).toFloat() // top-down image axis

        return relativeX to relativeY
    }

    private fun rotateCoordinates(x: Double, z: Double, degrees: Int): Pair<Double, Double> {
        val radians = Math.toRadians(degrees.toDouble())
        val cos = cos(radians)
        val sin = sin(radians)

        val rotX = x * cos - z * sin
        val rotZ = x * sin + z * cos

        return rotX to rotZ
    }

    /**
     * Converts quaternion (x,y,z,w) to yaw (rotation around vertical axis), in radians
     */
    private fun quaternionToYaw(qx: Double, qy: Double, qz: Double, qw: Double): Double {
        // Formula for yaw from quaternion
        val siny_cosp = 2.0 * (qw * qy + qx * qz)
        val cosy_cosp = 1.0 - 2.0 * (qy * qy + qx * qx)
        return atan2(siny_cosp, cosy_cosp)
    }
}
