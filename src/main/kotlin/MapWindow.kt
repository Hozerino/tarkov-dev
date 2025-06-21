import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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

@Composable
fun MapWindow() {
    val map = UIHandler.currentMap
    val dotPos = UIHandler.relativePosition

    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    imageSize = coordinates.size
                }
                .aspectRatio(1f) // maintain square aspect ratio or image ratio if known
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
                    drawCircle(
                        color = Color.Red,
                        radius = 8f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}
