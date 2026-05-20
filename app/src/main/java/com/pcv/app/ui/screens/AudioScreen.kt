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
import com.pcv.app.data.ScheduledMedia
import com.pcv.app.service.MediaSupervisorService
import com.pcv.app.ui.theme.CyanPrimary

@Composable
fun AudioScreen(service: MediaSupervisorService?) {
    var url       by remember { mutableStateOf("") }
    var title     by remember { mutableStateOf("") }
    var volume    by remember { mutableFloatStateOf(0.8f) }
    var loop      by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { service?.onStatusChange = { isPlaying = service.playerManager.isAudioPlaying } }

    PCVScreen(title = "Audio / Music") {
        PCVCard {
            OutlinedTextField(url,   { url = it },   label = { Text("Stream URL (.mp3, .aac, HLS…)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(title, { title = it }, label = { Text("Title (optional)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Volume ${(volume * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(value = volume, onValueChange = { volume = it; service?.playerManager?.setAudioVolume(it) },
                    modifier = Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = loop, onCheckedChange = { loop = it })
                Spacer(Modifier.width(8.dp))
                Text("Loop", style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    if (url.isNotBlank()) service?.playerManager?.playMedia(
                        MediaConfig(url = url, type = MediaType.AUDIO, title = title, volume = volume, loop = loop))
                }, modifier = Modifier.weight(1f), enabled = url.isNotBlank()) {
                    Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(4.dp)); Text("Play")
                }
                OutlinedButton(onClick = {
                    if (url.isNotBlank()) service?.scheduler?.enqueue(
                        ScheduledMedia(mediaConfig = MediaConfig(url = url, type = MediaType.AUDIO, title = title)))
                }, modifier = Modifier.weight(1f), enabled = url.isNotBlank()) {
                    Icon(Icons.Filled.Queue, null); Spacer(Modifier.width(4.dp)); Text("Queue")
                }
            }
        }
        if (isPlaying) {
            PCVCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.GraphicEq, null, tint = CyanPrimary)
                    Text(service?.playerManager?.audioTitle ?: "", style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f), maxLines = 1)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { service?.playerManager?.pauseAudio() }, modifier = Modifier.weight(1f)) {
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
