# GAMA (Gamma Music Frequency Platform)

[![Android](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20BOM%202024.06.00-purple.svg)](https://developer.android.com/jetpack/compose)
[![Audio](https://img.shields.io/badge/Engine-Official%20YouTube%20IFrame%20%2B%20Biquad%20DSP-orange.svg)]()

**GAMA** is a high-performance modern Android music and streaming audio platform built with Jetpack Compose, Material 3, and advanced audio processing engines.

Developed by **VHUWON MATHERS**.

---

## Features

- **Genuine Audio & Video Playback**: Integrated official hardware-accelerated YouTube IFrame embed engine with zero background crashes, unmuted commercial tracks, and accurate audio playback.
- **Floating Mini-Player & PiP**: Compact floating player docked seamlessly above bottom navigation bar with real-time video thumbnail preview.
- **Smart Queue & Recommendation Engine**: Intelligent recommendation pipeline analyzing playback history and matching artist/genre affinities dynamically.
- **Studio-Grade 5-Band Equalizer**: Custom second-order Biquad IIR filter implementation (Direct Form II) with presets (Flat, Bass Boost, Vocal, Club, Treble, Electronic) and live 16-band waveform visualizer.
- **Offline & Local Download Capability**: Download tracks directly for offline listening with automated local storage caching.
- **Background Playback & Lock Screen Controls**: Powered by Android Foreground Service (`GamaPlaybackService`) with `MediaSessionCompat` integration for full lock-screen and notification shade playback controls.
- **Universal Device Compatibility**: Supports Android 7.0 (Nougat, API 24) all the way through Android 15+.

---

## Architecture

- **Language**: Kotlin 2.0.0
- **UI Toolkit**: Jetpack Compose with Material 3 Design
- **Local Storage**: Room Database (`GammaDatabase` v4) with SQLite reactive flows
- **Networking**: OkHttp3 with YouTube InnerTube & oEmbed metadata pipelines
- **Media Engine**: `GamaAudioEngine` (Biquad DSP + Android MediaPlayer) & `YouTubeWebViewBridge`
- **Dependency Injection**: Unidirectional Service Container (`GammaContainer`)

---

## Building & Installing

### Prerequisites
- JDK 17 or JDK 21
- Android SDK Platform 34 (Build Tools 34.0.0+)
- Gradle 8.7+

### Build Debug APK
```bash
./gradlew assembleDebug
```
The output APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## License & Platform Notice
Respects all YouTube Terms of Service and content creator rights via compliant embedded IFrame players.
