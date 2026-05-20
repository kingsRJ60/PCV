package com.pcv.app.data

enum class MediaType { AUDIO, VIDEO, AD }

data class MediaConfig(
    val url           : String,
    val type          : MediaType,
    val title         : String  = "",
    val volume        : Float   = 1.0f,
    val loop          : Boolean = false,
    val backgroundMode: Boolean = false
)

enum class BannerPosition { TOP, BOTTOM }

data class BannerConfig(
    val message        : String,
    val durationMs     : Long           = 5_000L,
    val position       : BannerPosition = BannerPosition.TOP,
    val backgroundColor: Int            = 0xCC000000.toInt(),
    val textColor      : Int            = 0xFFFFFFFF.toInt()
)

data class VideoOverlayConfig(
    val widthPx  : Int = 640,
    val heightPx : Int = 360,
    val marginPx : Int = 24
)

data class ScheduledMedia(
    val id          : String        = "",
    val mediaConfig : MediaConfig?  = null,
    val bannerConfig: BannerConfig? = null,
    val delayMs     : Long          = 0L,
    val intervalMs  : Long          = 0L
)

enum class ImprovementCategory { Performance, Architecture, Memory, Security, BestPractice }
enum class ImprovementSeverity { Critical, Warning, Suggestion }

data class CodeImprovement(
    val category    : ImprovementCategory,
    val severity    : ImprovementSeverity,
    val title       : String,
    val description : String,
    val lineRange   : String = "",
    val originalCode: String = "",
    val improvedCode: String = ""
)

data class SourceFile(
    val name  : String,
    val path  : String,
    val module: String
)
