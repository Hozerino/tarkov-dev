import androidx.compose.ui.input.key.Key.Companion.T
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*

fun main() = runBlocking {
    application {
        Window(onCloseRequest = ::exitApplication, title = "Tarkov Plotter") {
            MapWindow()
        }

        ScreenshotWatcher.start()
        // factory com Z=1 eh provavelmente leste ou oeste em Factory (rotation 90)
        UIHandler.setMap("factory")
        UIHandler.plot(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0) // Example position
    }
}
