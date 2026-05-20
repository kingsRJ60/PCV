package com.pcv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pcv.app.data.*
import com.pcv.app.service.MediaSupervisorService

@Composable
fun AdsScreen(service: MediaSupervisorService?) {
    var bannerMsg    by remember { mutableStateOf("") }
    var bannerSec    by remember { mutableStateOf("5") }
    var bannerBottom by remember { mutableStateOf(false) }
    var adUrl        by remember { mutableStateOf("") }

    PCVScreen(title = "Ads & Banners") {
        PCVCard {
            Text("Text Banner", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(bannerMsg, { bannerMsg = it }, label = { Text("Message") },
                modifier = Modifier.fillMaxWidth(), maxLines = 2)
            OutlinedTextField(bannerSec, { bannerSec = it.filter(Char::isDigit) },
                label = { Text("Duration (seconds)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = bannerBottom, onCheckedChange = { bannerBottom = it })
                Spacer(Modifier.width(8.dp))
                Text(if (bannerBottom) "Bottom" else "Top", style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = {
                if (bannerMsg.isNotBlank()) {
                    val ms  = (bannerSec.toLongOrNull() ?: 5L) * 1000L
                    val pos = if (bannerBottom) BannerPosition.BOTTOM else BannerPosition.TOP
                    service?.overlayManager?.showBanner(BannerConfig(bannerMsg, ms, pos), service.serviceScope)
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = bannerMsg.isNotBlank()) {
                Icon(Icons.Filled.Notifications, null)
                Spacer(Modifier.width(6.dp)); Text("Show Banner")
            }
        }
        PCVCard {
            Text("Video Ad", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(adUrl, { adUrl = it }, label = { Text("Ad video URL") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(onClick = {
                if (adUrl.isNotBlank()) {
                    service?.playerManager?.playMedia(MediaConfig(url = adUrl, type = MediaType.AD))
                    service?.overlayManager?.showBanner(
                        BannerConfig("Ad playing…", 10_000L, BannerPosition.BOTTOM), service.serviceScope)
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = adUrl.isNotBlank()) {
                Icon(Icons.Filled.Campaign, null); Spacer(Modifier.width(6.dp)); Text("Play Ad + Banner")
            }
        }
        OutlinedButton(onClick = { service?.overlayManager?.removeAll() },
            modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.ClearAll, null); Spacer(Modifier.width(6.dp)); Text("Remove All Overlays")
        }
    }
}
