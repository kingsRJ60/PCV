package com.pcv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pcv.app.data.*
import com.pcv.app.service.MediaSupervisorService
import com.pcv.app.ui.theme.*

@Composable
fun SchedulerScreen(service: MediaSupervisorService?) {
    var mediaUrl    by remember { mutableStateOf("") }
    var scheduleId  by remember { mutableStateOf("") }
    var intervalSec by remember { mutableStateOf("60") }
    var delaySec    by remember { mutableStateOf("0") }
    var withBanner  by remember { mutableStateOf(false) }
    var bannerMsg   by remember { mutableStateOf("Scheduled content playing…") }
    var scheduleIds by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        service?.onStatusChange = { scheduleIds = service.scheduler.activeScheduleIds }
    }

    PCVScreen(title = "Scheduler") {
        PCVCard {
            Text("New Schedule", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(scheduleId,  { scheduleId  = it }, label = { Text("Schedule ID (unique)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(mediaUrl,    { mediaUrl    = it }, label = { Text("Media URL") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(delaySec,    { delaySec    = it.filter(Char::isDigit) },
                label = { Text("Initial delay (seconds)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(intervalSec, { intervalSec = it.filter(Char::isDigit) },
                label = { Text("Repeat interval (seconds), 0 = once") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = withBanner, onCheckedChange = { withBanner = it })
                Spacer(Modifier.width(8.dp))
                Text("Show banner when triggered", style = MaterialTheme.typography.bodyMedium)
            }
            if (withBanner) {
                OutlinedTextField(bannerMsg, { bannerMsg = it }, label = { Text("Banner message") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            Button(onClick = {
                if (scheduleId.isNotBlank() && mediaUrl.isNotBlank()) {
                    service?.scheduler?.schedule(ScheduledMedia(
                        id           = scheduleId,
                        mediaConfig  = MediaConfig(url = mediaUrl, type = MediaType.AUDIO),
                        bannerConfig = if (withBanner) BannerConfig(bannerMsg) else null,
                        delayMs      = (delaySec.toLongOrNull() ?: 0L) * 1000L,
                        intervalMs   = (intervalSec.toLongOrNull() ?: 0L) * 1000L
                    ))
                    scheduleIds = service?.scheduler?.activeScheduleIds ?: emptyList()
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = scheduleId.isNotBlank() && mediaUrl.isNotBlank()) {
                Icon(Icons.Filled.Alarm, null); Spacer(Modifier.width(6.dp)); Text("Activate Schedule")
            }
        }
        if (scheduleIds.isNotEmpty()) {
            PCVCard {
                Text("Active (${scheduleIds.size})", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary)
                scheduleIds.forEach { id ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.RadioButtonChecked, null, tint = GreenActive, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(id, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = {
                            service?.scheduler?.cancel(id)
                            scheduleIds = service?.scheduler?.activeScheduleIds ?: emptyList()
                        }) { Icon(Icons.Filled.Cancel, null, tint = RedAlert) }
                    }
                }
                OutlinedButton(onClick = {
                    service?.scheduler?.cancelAll(); scheduleIds = emptyList()
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.ClearAll, null); Spacer(Modifier.width(6.dp)); Text("Cancel All")
                }
            }
        }
    }
}
