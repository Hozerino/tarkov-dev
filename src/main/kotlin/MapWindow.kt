import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
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
    var scale by remember { mutableStateOf(1f) }

    var expanded by remember { mutableStateOf(false) }
    val mapOptions = MapRegistry.maps.keys.sorted()
    var selectedMap by remember { mutableStateOf(map.name) }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Dropdown ---
        Box(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
        ) {
            Column {
                Button(onClick = { expanded = true }) {
                    Text(selectedMap)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    mapOptions.forEach { mapKey ->
                        DropdownMenuItem(onClick = {
                            selectedMap = MapRegistry.maps[mapKey]!!.name
                            UIHandler.setMap(mapKey)
                            expanded = false
                        }) {
                            Text(MapRegistry.maps[mapKey]!!.name)
                        }
                    }
                }
            }
        }

        // --- Map + Canvas Layer ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .onGloballyPositioned { coordinates -> imageSize = coordinates.size }
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val scroll = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                                if (scroll != 0f) {
                                    scale = (scale - scroll * 0.01f).coerceIn(0.2f, 5f)
                                }
                            }
                        }
                    }
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            ) {
                Image(
                    painter = painterResource("images/${map.imageFile}"),
                    contentDescription = map.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                if (dotPos != null && imageSize.width > 0 && imageSize.height > 0) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val x = dotPos.first * size.width
                        val y = dotPos.second * size.height

                        drawCircle(
                            color = Color.Red,
                            radius = 8f,
                            center = Offset(x, y)
                        )

                        lookQuat?.let { (qx, qy, qz, qw) ->
                            val yawRad = quaternionToYawRad(qx, qy, qz, qw)
                            val adjustedYaw = yawRad + Math.toRadians(map.coordinateRotation.toDouble())

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

fun isValidQuaternion(w: Double, x: Double, y: Double, z: Double, epsilon: Double = 1e-5): Boolean {
    val magnitudeSquared = w * w + x * x + y * y + z * z
    return kotlin.math.abs(magnitudeSquared - 1.0) < epsilon
}


fun quaternionToYawRad(qx: Double, qy: Double, qz: Double, qw: Double): Double {
    val (w, x, y, z) = if (!isValidQuaternion(w = qw, x = qx, y = qy, z = qz)) {
        println("Warning: Invalid quaternion, resetting to identity")
        listOf(1.0, 0.0, 0.0, 0.0)
    } else {
        listOf(qw, qx, qy, qz)
    }

    return atan2(
        2.0 * (w * y + x * z),
        1.0 - 2.0 * (y * y + z * z)
    )
}
