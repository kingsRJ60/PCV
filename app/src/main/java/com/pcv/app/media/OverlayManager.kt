package com.pcv.app.media

import android.content.Context
import android.graphics.PixelFormat
import android.view.*
import android.widget.TextView
import androidx.media3.ui.PlayerView
import com.pcv.app.data.BannerConfig
import com.pcv.app.data.BannerPosition
import com.pcv.app.data.VideoOverlayConfig
import kotlinx.coroutines.*

class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val activeViews   = mutableListOf<View>()
    var activeBannerCount: Int = 0; private set

    fun showBanner(config: BannerConfig, scope: CoroutineScope) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = if (config.position == BannerPosition.TOP) Gravity.TOP else Gravity.BOTTOM
        }
        val view = TextView(context).apply {
            text = config.message; textSize = 15f
            setPadding(36, 22, 36, 22)
            setTextColor(config.textColor)
            setBackgroundColor(config.backgroundColor)
            gravity = Gravity.CENTER
        }
        safeAdd(view, params)
        activeBannerCount++
        scope.launch {
            delay(config.durationMs)
            safeRemove(view)
            activeBannerCount = maxOf(0, activeBannerCount - 1)
        }
    }

    fun createVideoOverlay(config: VideoOverlayConfig = VideoOverlayConfig()): PlayerView {
        val params = WindowManager.LayoutParams(
            config.widthPx, config.heightPx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.OPAQUE
        ).apply { gravity = Gravity.BOTTOM or Gravity.END; x = config.marginPx; y = config.marginPx }
        val pv = PlayerView(context).apply { useController = false }
        safeAdd(pv, params)
        return pv
    }

    fun removeAll() {
        activeViews.toList().forEach { safeRemove(it) }
        activeViews.clear(); activeBannerCount = 0
    }

    private fun safeAdd(view: View, params: ViewGroup.LayoutParams) {
        try { windowManager.addView(view, params); activeViews.add(view) }
        catch (e: Exception) { e.printStackTrace() }
    }

    private fun safeRemove(view: View) {
        try { windowManager.removeView(view); activeViews.remove(view) }
        catch (_: Exception) { }
    }
}
