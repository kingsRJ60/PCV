package com.pcv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pcv.app.data.MediaConfig
import com.pcv.app.data.MediaType
import com.pcv.app.service.MediaSupervisorService

@Composable
fun VideoScreen(service: MediaSupervisorService?) {
    var url       by remember { mutableStateOf("") }
    var silent    by remember { mutableStateOf(false) }
    var volume    by remember { mutableFloatStateOf(1.0f) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { service?.onStatusChange = { isPlaying = service.playerManager.isVideoPlaying } }

    PCVScreen(title = "Video") {
        PCVCard {
            OutlinedTextField(url, { url = it }, label = { Text("Video URL (.mp4, .m3u8, DASH…)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = silent, onCheckedChange = { silent = it })
                Spacer(Modifier.width(8.dp))
                Text("Silent / Background mode", style = MaterialTheme.typography.bodyMedium)
            }
            if (!silent) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Volume ${(volume * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(value = volume, onValueChange = { volume = it; service?.playerManager?.setVideoVolume(it) },
                        modifier = Modifier.weight(1f))
                }
            }
            Button(onClick = {
                if (url.isNotBlank()) service?.playerManager?.playMedia(
                    MediaConfig(url = url, type = MediaType.VIDEO,
                        volume = if (silent) 0f else volume, backgroundMode = silent))
            }, modifier = Modifier.fillMaxWidth(), enabled = url.isNotBlank()) {
                Icon(Icons.Filled.PlayCircle, null); Spacer(Modifier.width(4.dp)); Text("Play Video Overlay")
            }
        }
        if (isPlaying) {
            PCVCard {
                Text("Video is playing", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { service?.playerManager?.pauseVideo() }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Pause, null); Spacer(Modifier.width(4.dp)); Text("Pause")
                    }
                    OutlinedButton(onClick = { service?.playerManager?.stopAll() }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Stop, null); Spacer(Modifier.width(4.dp)); Text("Stop")
                    }
                }
            }
        }
    }
}
