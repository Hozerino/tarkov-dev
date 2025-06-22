import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition.PlatformDefault.x
import androidx.compose.ui.window.WindowPosition.PlatformDefault.y
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MapWindow() {
    val map = UIHandler.currentMap
    val dotPos = UIHandler.relativePosition
    val lookQuat = UIHandler.lookQuaternion

    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    fun quaternionToForwardVector(x: Double, y: Double, z: Double, w: Double): Triple<Double, Double, Double> {
        // Converts quaternion to a forward (look) vector in 3D space
        val fx = 2 * (x * z + w * y)
        val fy = 2 * (y * z - w * x)
        val fz = 1 - 2 * (x * x + y * y)
        return Triple(fx, fy, fz)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    imageSize = coordinates.size
                }
                .aspectRatio(1f)
        ) {
            Image(
                painter = painterResource("images/${map.imageFile}"),
                contentDescription = map.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            if (dotPos != null && imageSize.width > 0 && imageSize.height > 0) {
                Canvas(modifier = Modifier.size(imageSize.width.dp, imageSize.height.dp)) {
                    val x = dotPos.first * size.width
                    val y = dotPos.second * size.height

                    // Draw position dot
                    drawCircle(
                        color = Color.Red,
                        radius = 8f,
                        center = Offset(x, y)
                    )

                    lookQuat?.let { (qx, qy, qz, qw) ->
                        val forward = quaternionToForwardVector(qx, qy, qz, qw)

                        // Flatten onto XZ plane
                        var dx = forward.first
                        var dz = forward.third

                        // Apply map coordinateRotation
                        val rotationRad = Math.toRadians(map.coordinateRotation.toDouble())
                        val rotatedDx = dx * cos(rotationRad) - dz * sin(rotationRad)
                        val rotatedDz = dx * sin(rotationRad) + dz * cos(rotationRad)

                        // Convert to screen space (Z is top of screen)
                        val lineLength = 40f
                        val endX = x + (rotatedDx * lineLength).toFloat()
                        val endY = y - (rotatedDz * lineLength).toFloat()

                        drawLine(
                            color = Color.Blue,
                            start = Offset(x, y),
                            end = Offset(endX, endY),
                            strokeWidth = 3f
                        )
                    }
                }
            }
        }
    }
}
