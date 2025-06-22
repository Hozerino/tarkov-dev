import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.runBlocking
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier

fun main() = runBlocking {
    application {
        Window(onCloseRequest = ::exitApplication, title = "Tarkov Plotter") {
            var selectedMap by remember { mutableStateOf("factory") }
            var screenshotFolder by remember { mutableStateOf(ScreenshotWatcher.watchDir.toAbsolutePath().toString()) }

            // Initial setup
            LaunchedEffect(Unit) {
                ScreenshotWatcher.start()
                UIHandler.setMap(selectedMap)
                UIHandler.plot(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0)
            }

            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(
                    selectedMap = selectedMap,
                    mapOptions = MapRegistry.maps.keys.sorted(),
                    screenshotFolder = screenshotFolder,
                    onMapSelected = { mapKey ->
                        selectedMap = mapKey
                        UIHandler.setMap(mapKey)
                    },
                    onFolderChanged = { path ->
                        screenshotFolder = path
                        ScreenshotWatcher.setWatchDir(path)
                    }
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    MapWindow()
                }
            }
        }
    }
}
