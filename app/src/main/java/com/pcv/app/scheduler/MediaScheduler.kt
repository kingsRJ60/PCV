package com.pcv.app.scheduler

import com.pcv.app.data.ScheduledMedia
import com.pcv.app.media.ExoPlayerManager
import com.pcv.app.media.OverlayManager
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue

class MediaScheduler(
    private val playerManager : ExoPlayerManager,
    private val overlayManager: OverlayManager,
    private val scope         : CoroutineScope
) {
    private val namedJobs = mutableMapOf<String, Job>()
    private val queue     = ConcurrentLinkedQueue<ScheduledMedia>()
    private var queueJob : Job? = null

    val activeScheduleIds: List<String> get() = namedJobs.keys.toList()
    val queueSize        : Int          get() = queue.size

    fun schedule(item: ScheduledMedia) {
        require(item.id.isNotBlank()) { "id must not be blank" }
        namedJobs[item.id]?.cancel()
        namedJobs[item.id] = scope.launch {
            delay(item.delayMs)
            execute(item)
            if (item.intervalMs > 0) {
                while (isActive) { delay(item.intervalMs); execute(item) }
            }
        }
    }

    fun cancel(id: String) { namedJobs[id]?.cancel(); namedJobs.remove(id) }

    fun cancelAll() {
        namedJobs.values.forEach { it.cancel() }
        namedJobs.clear(); queue.clear()
    }

    fun enqueue(item: ScheduledMedia) {
        queue.add(item)
        if (queueJob?.isActive != true) startQueue()
    }

    fun clearQueue() { queue.clear() }

    private fun startQueue() {
        queueJob = scope.launch {
            while (queue.isNotEmpty() && isActive) {
                queue.poll()?.let { execute(it) }
                delay(300)
            }
        }
    }

    private suspend fun execute(item: ScheduledMedia) {
        item.mediaConfig?.let  { playerManager.playMedia(it) }
        item.bannerConfig?.let { overlayManager.showBanner(it, scope) }
    }
}
