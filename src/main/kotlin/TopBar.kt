import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TopBar(
    selectedMap: String,
    mapOptions: List<String>,
    screenshotFolder: String,
    onMapSelected: (String) -> Unit,
    onFolderChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xAAFFFFFF))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box {
            Button(onClick = { expanded = true }) {
                Text(selectedMap)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                mapOptions.forEach { mapKey ->
                    DropdownMenuItem(onClick = {
                        onMapSelected(mapKey)
                        expanded = false
                    }) {
                        Text(MapRegistry.maps[mapKey]!!.name)
                    }
                }
            }
        }

        TextField(
            value = screenshotFolder,
            onValueChange = onFolderChanged,
            label = { Text("Screenshot Folder") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }
}
