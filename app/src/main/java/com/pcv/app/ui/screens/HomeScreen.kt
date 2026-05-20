package com.pcv.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.pcv.app.service.MediaSupervisorService
import com.pcv.app.ui.theme.*

@Composable
fun HomeScreen(service: MediaSupervisorService?) {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { service?.onStatusChange = { tick++ } }

    val isAudio    = service?.playerManager?.isAudioPlaying ?: false
    val isVideo    = service?.playerManager?.isVideoPlaying ?: false
    val banners    = service?.overlayManager?.activeBannerCount ?: 0
    val schedules  = service?.scheduler?.activeScheduleIds?.size ?: 0
    val audioTitle = service?.playerManager?.audioTitle ?: ""

    PCVScreen(title = "") {
        Column {
            Text("PCV", style = MaterialTheme.typography.displaySmall,
                color = CyanPrimary, fontFamily = FontFamily.Monospace)
            Text("Primary Control over Viewing",
                style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }

        Text("Live Status", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatusTile("Audio",    Icons.Filled.MusicNote,   isAudio,          Modifier.weight(1f))
            StatusTile("Video",    Icons.Filled.PlayCircle,  isVideo,          Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatusTile("Banners ($banners)",   Icons.Filled.Campaign, banners > 0,   Modifier.weight(1f))
            StatusTile("Schedules ($schedules)", Icons.Filled.Schedule, schedules > 0, Modifier.weight(1f))
        }

        if (isAudio && audioTitle.isNotBlank()) {
            Surface(shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.GraphicEq, null, tint = CyanPrimary)
                    Column {
                        Text("Now Playing", style = MaterialTheme.typography.labelSmall, color = CyanOnContainer)
                        Text(audioTitle, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, maxLines = 1)
                    }
                }
            }
        }

        if (isAudio || isVideo || banners > 0 || schedules > 0) {
            Button(
                onClick  = { service?.playerManager?.stopAll(); service?.scheduler?.cancelAll() },
                colors   = ButtonDefaults.buttonColors(containerColor = RedAlert),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.StopCircle, null)
                Spacer(Modifier.width(8.dp))
                Text("Stop Everything")
            }
        }
    }
}

@Composable
private fun StatusTile(label: String, icon: ImageVector, active: Boolean, modifier: Modifier) {
    val bg     = if (active) GreenActive.copy(alpha = 0.12f) else NavyCard
    val tint   = if (active) GreenActive else TextSecondary
    val border = if (active) GreenActive.copy(alpha = 0.5f) else NavyBorder
    Surface(shape = RoundedCornerShape(12.dp), color = bg,
        modifier = modifier.border(1.dp, border, RoundedCornerShape(12.dp))) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
            Text(if (active) "Active" else "Idle",
                style = MaterialTheme.typography.bodySmall, color = tint.copy(alpha = 0.7f))
        }
    }
}
