import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Tarkov Plotter") {
        MapWindow()
    }

    LogsWatcher.start()
    ScreenshotWatcher.start()
    UIHandler.setMap("customs") // Optional right now, "customs" is the default
    UIHandler.plot(0.0, 0.0, 0.0) // Example position
}