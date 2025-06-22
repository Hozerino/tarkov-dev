import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import java.awt.FileDialog
import java.awt.Frame
import kotlin.math.*

@Composable
fun MapWindow() {
    val map = UIHandler.currentMap
    val dotPos = UIHandler.relativePosition
    val lookQuat = UIHandler.lookQuaternion

    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    var lastDragPos by remember { mutableStateOf(Offset.Zero) }

    var expanded by remember { mutableStateOf(false) }
    val mapOptions = MapRegistry.maps.keys.sorted()
    var selectedMap by remember { mutableStateOf(map.name) }

    var screenshotFolder by remember { mutableStateOf(ScreenshotWatcher.watchDir.toAbsolutePath().toString()) }
    var showFolderDialog by remember { mutableStateOf(false) }

    // Folder picker using AWT dialog (Desktop Compose)
    if (showFolderDialog) {
        LaunchedEffect(Unit) {
            val fd = FileDialog(Frame(), "Select Screenshot Folder", FileDialog.LOAD)
            fd.isVisible = true
            val dir = fd.directory
            if (dir != null) {
                screenshotFolder = dir
                ScreenshotWatcher.setWatchDir(dir)
            }
            showFolderDialog = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Dropdown & Folder selector UI
        Column(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
                .background(Color(0xAAFFFFFF))
                .padding(8.dp)
        ) {
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

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { showFolderDialog = true }) {
                Text("Select Screenshot Folder")
            }
            BasicText("Watching: $screenshotFolder", modifier = Modifier.padding(top = 4.dp))
        }

        // Map + Canvas with zoom and pan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp)
                .pointerInput(Unit) {
                    // Zoom with mouse wheel scroll
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val scroll = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                            if (scroll != 0f) {
                                scale = (scale - scroll * 0.03f).coerceIn(0.2f, 10f)
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        var draggingPointerId: PointerId? = null
                        while (true) {
                            val event = awaitPointerEvent()
                            val pressed = event.buttons.isPrimaryPressed

                            if (draggingPointerId != null) {
                                val dragChange = event.changes.find { it.id == draggingPointerId }
                                if (dragChange != null && dragChange.pressed) {
                                    val delta = dragChange.positionChange()
                                    offset += delta
                                    dragChange.consume()
                                } else {
                                    draggingPointerId = null
                                }
                            } else {
                                if (pressed) {
                                    val downChange = event.changes.firstOrNull { it.pressed && !it.previousPressed }
                                    if (downChange != null) {
                                        draggingPointerId = downChange.id
                                    }
                                }
                            }
                        }
                    }
                },
                contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .onGloballyPositioned { coordinates -> imageSize = coordinates.size }
                    .aspectRatio(1f)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
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

fun isValidQuaternion(w: Double, x: Double, y: Double, z: Double, epsilon: Double = 1e-6): Boolean {
    val normSquared = w * w + x * x + y * y + z * z
    return kotlin.math.abs(1.0 - normSquared) < epsilon
}

fun quaternionToYawRad(qx: Double, qy: Double, qz: Double, qw: Double): Double {
    val (w, x, y, z) = if (!isValidQuaternion(w = qw, x = qx, y = qy, z = qz)) {
        println("Warning: Invalid quaternion, resetting to identity")
        listOf(1.0, 0.0, 0.0, 0.0)
    } else {
        listOf(qw, qx, qy, qz)
    }

    return kotlin.math.atan2(
        2.0 * (w * y + x * z),
        1.0 - 2.0 * (y * y + z * z)
    )
}
