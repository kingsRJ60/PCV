package com.pcv.app.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.pcv.app.data.MediaConfig
import com.pcv.app.data.MediaType

class ExoPlayerManager(
    private val context: Context,
    private val onStateChange: () -> Unit = {}
) {
    private val sysAudio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private var audioPlayer: ExoPlayer? = null
    private var videoPlayer: ExoPlayer? = null

    var audioTitle    : String  = ""; private set
    var isAudioPlaying: Boolean = false; private set
    var isVideoPlaying: Boolean = false; private set

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS                    -> pauseAudio()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT          -> pauseAudio()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> setAudioVolume(0.2f)
            AudioManager.AUDIOFOCUS_GAIN                    -> { resumeAudio(); setAudioVolume(1f) }
        }
    }

    fun playMedia(config: MediaConfig) = when (config.type) {
        MediaType.AUDIO      -> playAudio(config)
        MediaType.VIDEO,
        MediaType.AD         -> playVideo(config)
    }

    fun pauseAudio()  { audioPlayer?.pause(); isAudioPlaying = false; onStateChange() }
    fun resumeAudio() { audioPlayer?.play();  isAudioPlaying = true;  onStateChange() }
    fun pauseVideo()  { videoPlayer?.pause(); isVideoPlaying = false; onStateChange() }
    fun resumeVideo() { videoPlayer?.play();  isVideoPlaying = true;  onStateChange() }

    fun stopAll() {
        audioPlayer?.stop(); videoPlayer?.stop()
        isAudioPlaying = false; isVideoPlaying = false
        audioTitle = ""; onStateChange()
    }

    fun setAudioVolume(v: Float) { audioPlayer?.volume = v.coerceIn(0f, 1f) }
    fun setVideoVolume(v: Float) { videoPlayer?.volume = v.coerceIn(0f, 1f) }
    fun getVideoPlayer(): ExoPlayer? = videoPlayer

    fun release() {
        focusRequest?.let { sysAudio.abandonAudioFocusRequest(it) }
        audioPlayer?.release(); audioPlayer = null
        videoPlayer?.release(); videoPlayer = null
    }

    private fun playAudio(config: MediaConfig) {
        if (!requestAudioFocus()) return
        audioPlayer = (audioPlayer ?: ExoPlayer.Builder(context).build().also { p ->
            p.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isAudioPlaying = playing; onStateChange()
                }
            })
        })
        audioPlayer!!.apply {
            repeatMode = if (config.loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            setMediaItem(MediaItem.fromUri(config.url))
            volume = config.volume; prepare(); play()
        }
        audioTitle = config.title.ifBlank { config.url.substringAfterLast('/') }
        onStateChange()
    }

    private fun playVideo(config: MediaConfig) {
        videoPlayer = (videoPlayer ?: ExoPlayer.Builder(context).build().also { p ->
            p.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isVideoPlaying = playing; onStateChange()
                }
            })
        })
        videoPlayer!!.apply {
            repeatMode = if (config.loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            setMediaItem(MediaItem.fromUri(config.url))
            volume = if (config.backgroundMode) 0f else config.volume
            prepare(); play()
        }
        onStateChange()
    }

    private fun requestAudioFocus(): Boolean {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        focusRequest = AudioFocusRequest
            .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attrs)
            .setOnAudioFocusChangeListener(focusListener)
            .build()
        return sysAudio.requestAudioFocus(focusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }
}
