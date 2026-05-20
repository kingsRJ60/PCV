package com.pcv.app.codelab

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pcv.app.data.CodeImprovement
import com.pcv.app.data.ImprovementCategory
import com.pcv.app.data.ImprovementSeverity
import com.pcv.app.data.SourceFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

val PCV_SOURCE_FILES = listOf(
    SourceFile("MediaSupervisorService.kt", "source/MediaSupervisorService.kt", "service"),
    SourceFile("ExoPlayerManager.kt",       "source/ExoPlayerManager.kt",       "media"),
    SourceFile("OverlayManager.kt",         "source/OverlayManager.kt",         "media"),
    SourceFile("MediaScheduler.kt",         "source/MediaScheduler.kt",         "scheduler"),
    SourceFile("CodeImprovementEngine.kt",  "source/CodeImprovementEngine.kt",  "codelab"),
    SourceFile("MediaModels.kt",            "source/MediaModels.kt",            "data"),
    SourceFile("MainActivity.kt",           "source/MainActivity.kt",           "ui"),
    SourceFile("Navigation.kt",             "source/Navigation.kt",             "ui"),
    SourceFile("HomeScreen.kt",             "source/HomeScreen.kt",             "ui"),
    SourceFile("AudioScreen.kt",            "source/AudioScreen.kt",            "ui"),
    SourceFile("VideoScreen.kt",            "source/VideoScreen.kt",            "ui"),
    SourceFile("AdsScreen.kt",              "source/AdsScreen.kt",              "ui"),
    SourceFile("SchedulerScreen.kt",        "source/SchedulerScreen.kt",        "ui"),
    SourceFile("CodeLabScreen.kt",          "source/CodeLabScreen.kt",          "ui"),
    SourceFile("SharedComponents.kt",       "source/SharedComponents.kt",       "ui"),
    SourceFile("Theme.kt",                  "source/Theme.kt",                  "ui")
)

sealed class AnalysisResult {
    data class Success(val improvements: List<CodeImprovement>) : AnalysisResult()
    data class Error(val message: String) : AnalysisResult()
}

class CodeImprovementEngine(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("pcv_settings", Context.MODE_PRIVATE)

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    var apiKey: String
        get()      = prefs.getString("anthropic_api_key", "") ?: ""
        set(value) = prefs.edit().putString("anthropic_api_key", value).apply()

    val hasApiKey: Boolean get() = apiKey.isNotBlank()

    fun loadSourceCode(file: SourceFile): String = runCatching {
        context.assets.open(file.path).bufferedReader().use { it.readText() }
    }.getOrElse { "// Source not found: ${file.path}" }

    suspend fun analyzeFile(file: SourceFile, sourceCode: String): AnalysisResult =
        withContext(Dispatchers.IO) {
            if (!hasApiKey) return@withContext AnalysisResult.Error("No API key configured.")

            val systemPrompt = """
                You are a senior Android/Kotlin engineer reviewing the PCV app.
                Return ONLY a valid JSON array (no markdown) with 3-8 items:
                [{"category":"Performance|Architecture|Memory|Security|BestPractice",
                  "severity":"Critical|Warning|Suggestion",
                  "title":"short title",
                  "description":"explanation",
                  "lineRange":"e.g. 10-25",
                  "originalCode":"snippet",
                  "improvedCode":"improved snippet"}]
            """.trimIndent()

            val body = JSONObject().apply {
                put("model", "claude-sonnet-4-20250514")
                put("max_tokens", 2048)
                put("system", systemPrompt)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "File: ${file.name}\n\n```kotlin\n$sourceCode\n```")
                    })
                })
            }.toString()

            val request = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            try {
                val response   = http.newCall(request).execute()
                val rawBody    = response.body?.string() ?: return@withContext AnalysisResult.Error("Empty response")
                if (!response.isSuccessful) return@withContext AnalysisResult.Error("API ${response.code}")
                val text = JSONObject(rawBody)
                    .getJSONArray("content").getJSONObject(0).getString("text")
                    .trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                parseImprovements(text)
            } catch (e: Exception) {
                AnalysisResult.Error(e.message ?: "Unknown error")
            }
        }

    private fun parseImprovements(json: String): AnalysisResult = try {
        val type = object : TypeToken<List<RawImprovement>>() {}.type
        val raw: List<RawImprovement> = gson.fromJson(json, type)
        AnalysisResult.Success(raw.map { r ->
            CodeImprovement(
                category     = mapCat(r.category),
                severity     = mapSev(r.severity),
                title        = r.title,
                description  = r.description,
                lineRange    = r.lineRange,
                originalCode = r.originalCode,
                improvedCode = r.improvedCode
            )
        })
    } catch (e: Exception) {
        AnalysisResult.Error("Parse error: ${e.message}")
    }

    private fun mapCat(s: String) = when (s.lowercase()) {
        "performance"  -> ImprovementCategory.Performance
        "architecture" -> ImprovementCategory.Architecture
        "memory"       -> ImprovementCategory.Memory
        "security"     -> ImprovementCategory.Security
        else           -> ImprovementCategory.BestPractice
    }

    private fun mapSev(s: String) = when (s.lowercase()) {
        "critical" -> ImprovementSeverity.Critical
        "warning"  -> ImprovementSeverity.Warning
        else       -> ImprovementSeverity.Suggestion
    }

    private data class RawImprovement(
        val category    : String = "",
        val severity    : String = "",
        val title       : String = "",
        val description : String = "",
        val lineRange   : String = "",
        val originalCode: String = "",
        val improvedCode: String = ""
    )
}
