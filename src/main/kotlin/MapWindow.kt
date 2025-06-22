import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import kotlin.math.*

@Composable
fun MapWindow() {
    val map = UIHandler.currentMap
    val dotPos = UIHandler.relativePosition
    val lookQuat = UIHandler.lookQuaternion

    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    // --- Dropdown setup ---
    var expanded by remember { mutableStateOf(false) }
    val mapOptions = MapRegistry.maps.keys.toList()
    var selectedMap by remember { mutableStateOf(map.name) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dropdown menu for map selection
        Box(modifier = Modifier.padding(16.dp)) {
            Button(onClick = { expanded = true }) {
                Text("Map: $selectedMap")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                mapOptions.forEach { mapKey ->
                    val displayName = MapRegistry.maps[mapKey]?.name ?: mapKey
                    DropdownMenuItem(onClick = {
                        selectedMap = displayName
                        UIHandler.setMap(mapKey)
                        expanded = false
                    }) {
                        Text(displayName)
                    }
                }
            }
        }

        // Map display
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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

                        // Draw player position dot
                        drawCircle(
                            color = Color.Red,
                            radius = 8f,
                            center = Offset(x, y)
                        )

                        // Draw look direction if available
                        lookQuat?.let { (qx, qy, qz, qw) ->
                            val yawRad = quaternionToYawRad(qx, qy, qz, qw)
                            val rotationOffsetRad = Math.toRadians(map.coordinateRotation.toDouble())
                            val adjustedYaw = yawRad + rotationOffsetRad

                            val lineLength = 30f
                            val endX = x + lineLength * sin(adjustedYaw).toFloat()
                            val endY = y - lineLength * cos(adjustedYaw).toFloat()

                            drawLine(
                                start = Offset(x, y),
                                end = Offset(endX, endY),
                                color = Color.Blue,
                                strokeWidth = 3f
                            )
                        }
                    }
                }
            }
        }
    }
}

// Validate quaternion
fun isValidQuaternion(w: Double, x: Double, y: Double, z: Double, epsilon: Double = 1e-6): Boolean {
    val normSquared = w * w + x * x + y * y + z * z
    return abs(1.0 - normSquared) < epsilon
}

// Convert quaternion to yaw (top-down view)
fun quaternionToYawRad(qx: Double, qy: Double, qz: Double, qw: Double): Double {
    val (w, x, y, z) = if (!isValidQuaternion(w = qw, x = qx, y = qy, z = qz)) {
        println("Warning: Invalid quaternion, using identity")
        listOf(1.0, 0.0, 0.0, 0.0)
    } else {
        listOf(qw, qx, qy, qz)
    }

    return atan2(
        2.0 * (w * z + x * y),
        1.0 - 2.0 * (y * y + z * z)
    )
}
