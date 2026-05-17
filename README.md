# PCV — Primary Control over Viewing

A production-ready Android app that supervises and controls background playback of
advertisements, music, video, and banner overlays — without harming the performance
of the foreground app.

---

## Features

| Screen | What it does |
|---|---|
| **Home** | Live dashboard: audio, video, banner, and schedule status |
| **Audio** | Stream/loop audio with AudioFocus-aware volume management |
| **Video** | Play video as a floating overlay (picture-in-picture style) |
| **Ads & Banners** | Show dismissible text banners + scheduled video ads |
| **Scheduler** | Named recurring jobs (e.g. "ad every 5 min") + FIFO queue |
| **Code Lab ✨** | AI-powered code review of the app's own source code |

---

## Code Lab — how it works

1. Open the **Code Lab** tab
2. Enter your [Anthropic API key](https://console.anthropic.com/)
3. Select any source file from the drop-down
4. Tap **Analyze with AI**

Claude (claude-sonnet-4-20250514) reads the bundled source, returns structured JSON
improvements categorised as Performance / Architecture / Memory / Security / Best Practice,
each with severity, description, line range, before/after code, and a copy button.

The source files are bundled in `assets/source/` at build time and re-read at runtime
by `CodeImprovementEngine`. They always reflect the version shipped in the APK.

---

## Build instructions

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 (bundled with Android Studio)
- Android SDK API 35

### Steps

```bash
# 1. Open the project
File → Open → select the PCV/ folder

# 2. Let Gradle sync (first run downloads ~500 MB)

# 3. Build a debug APK
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# 4. Build a release APK (requires a signing keystore)
./gradlew assembleRelease
```

Or via Android Studio: **Build → Build App Bundle / APK → Build APK**.

### Install directly to a connected device
```bash
./gradlew installDebug
```

---

## Permissions explained

| Permission | Why |
|---|---|
| `FOREGROUND_SERVICE` | Run the media service in the background |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Required on Android 14+ for media services |
| `SYSTEM_ALERT_WINDOW` | Draw banner overlays over other apps |
| `INTERNET` | Stream remote media URLs + Anthropic API calls |
| `POST_NOTIFICATIONS` | Show the persistent foreground notification |

---

## Architecture

```
MainActivity (Compose + Navigation)
    └── MediaSupervisorService (Foreground Service)
            ├── ExoPlayerManager   — dual ExoPlayer, AudioFocus
            ├── OverlayManager     — WindowManager overlays
            └── MediaScheduler     — Coroutine named jobs + FIFO queue

CodeLabScreen
    └── CodeImprovementEngine      — Anthropic API, asset reader, JSON parser
```

---

## Extending the app

- **Custom ad network**: implement an `AdProvider` interface and inject it into `MediaScheduler`
- **Media session**: add `MediaSession` to `MediaSupervisorService` for lock-screen controls
- **Room database**: persist scheduled items across reboots
- **Remote config**: fetch ad schedules from your own server
