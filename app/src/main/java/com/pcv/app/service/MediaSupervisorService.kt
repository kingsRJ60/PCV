package com.pcv.app.service

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pcv.app.MainActivity
import com.pcv.app.media.ExoPlayerManager
import com.pcv.app.media.OverlayManager
import com.pcv.app.scheduler.MediaScheduler
import kotlinx.coroutines.*

class MediaSupervisorService : Service() {

    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): MediaSupervisorService = this@MediaSupervisorService
    }

    val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    lateinit var playerManager : ExoPlayerManager
    lateinit var overlayManager: OverlayManager
    lateinit var scheduler     : MediaScheduler
    var onStatusChange: (() -> Unit)? = null

    override fun onCreate() {
        super.onCreate()
        playerManager  = ExoPlayerManager(this) { onStatusChange?.invoke() }
        overlayManager = OverlayManager(this)
        scheduler      = MediaScheduler(playerManager, overlayManager, serviceScope)
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE    -> playerManager.pauseAudio()
            ACTION_RESUME   -> playerManager.resumeAudio()
            ACTION_STOP_ALL -> { playerManager.stopAll(); scheduler.cancelAll() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        playerManager.release()
        overlayManager.removeAll()
    }

    private fun buildNotification(): Notification {
        val channelId = "pcv_media_channel"
        val manager   = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "PCV Media Control", NotificationManager.IMPORTANCE_LOW)
                    .apply { description = "PCV background media playback" }
            )
        }
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val pauseIntent = PendingIntent.getService(
            this, 1,
            Intent(this, MediaSupervisorService::class.java).setAction(ACTION_PAUSE),
            PendingIntent.FLAG_IMMUTABLE)
        val stopIntent = PendingIntent.getService(
            this, 2,
            Intent(this, MediaSupervisorService::class.java).setAction(ACTION_STOP_ALL),
            PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("PCV — Primary Control over Viewing")
            .setContentText("Managing background media")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_media_pause, "Pause", pauseIntent)
            .addAction(android.R.drawable.ic_delete, "Stop All", stopIntent)
            .build()
    }

    companion object {
        const val NOTIF_ID        = 101
        const val ACTION_PAUSE    = "com.pcv.app.PAUSE"
        const val ACTION_RESUME   = "com.pcv.app.RESUME"
        const val ACTION_STOP_ALL = "com.pcv.app.STOP_ALL"
    }
}
