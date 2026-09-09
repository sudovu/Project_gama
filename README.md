# GAMA (Cybernetic Music Frequency Platform)

[![Android](https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-brightgreen.svg)](https://developer.android.com)
[![Windows](https://img.shields.io/badge/Windows-10%20%2F%2011%20Desktop-0078D6.svg)](https://www.microsoft.com/windows)
[![iOS](https://img.shields.io/badge/iOS-16.0%2B%20SwiftUI-000000.svg)](https://developer.apple.com/ios/)
[![Audio](https://img.shields.io/badge/Engine-Official%20YouTube%20IFrame%20%2B%205--Band%20DSP-orange.svg)]()
[![Developer](https://img.shields.io/badge/Architect-VHUWON%20MATHERS-purple.svg)](https://github.com/sudovu)

**GAMA** is a cybernetic music and streaming frequency ecosystem engineered across **Android**, **Windows**, and **iOS**. Designed with high-performance frequency-tuned soundscapes (432Hz harmonic resonance), studio-grade 5-band DSP equalizers, background playback, and hardware media key/lock screen integration.

Developed & Architected by **VHUWON MATHERS**.

---

## 🌐 Tri-Platform Ecosystem Structure

The repository is modularized into three independent, platform-optimized codebases:

```
GAMMA/
├── app/                      # Android Native Application (Kotlin + Jetpack Compose)
├── windows/                  # Windows Desktop Application (Electron 31 + React 18 + Vite 5)
├── ios/                      # iOS Native Application (SwiftUI + AVFoundation + MediaPlayer)
├── GAMA-v1.0-Universal.apk   # Pre-built Universal Android APK (Android 7.0+)
└── README.md
```

---

## ⚡ Cross-Platform Feature Parity

| Feature | Android (`app/`) | Windows Desktop (`windows/`) | iOS (`ios/`) |
| :--- | :--- | :--- | :--- |
| **Language / Framework** | Kotlin 2.0 + Jetpack Compose | React 18 + Vite + Tailwind + Electron | Swift 5 + SwiftUI Native |
| **Playback Engine** | YouTube IFrame + WebView Bridge | YouTube IFrame + Web Audio API | `WKWebView` + YouTube IFrame API |
| **Background Playback** | Foreground Service + Notification | Electron Background Process | `AVAudioSessionCategoryPlayback` |
| **Hardware Controls** | `MediaSessionCompat` (Lock Screen) | Windows Media Keys IPC | `MPRemoteCommandCenter` (Lock Screen) |
| **Equalizer** | 5-Band Biquad IIR Filter | 5-Band Web Audio Biquad Filter | 5-Band Hardware DSP & Presets |
| **432Hz Tuning** | Natural Harmonic Resonator | Harmonic Frequency Shift | Core Resonant Alpha Alignment |
| **Local Vault** | Room Database v4 | `%APPDATA%\GamaMusic` JSON Vault | Encrypted Local Cache |
| **Video PiP** | Floating Mini-Player Bar | Toggleable Floating Video PiP | Embedded Mini & Fullscreen Video |
| **Developer Profile** | VHUWON MATHERS | VHUWON MATHERS | VHUWON MATHERS |

---

## 🚀 Platform Quick Starts

### 📱 1. Android
- **Prerequisites**: JDK 17/21, Android SDK Platform 34.
- **Build APK**:
  ```bash
  ./gradlew assembleDebug
  ```
- **Install Ready-To-Use Universal APK**:
  ```bash
  adb install GAMA-v1.0-Universal.apk
  ```
- See [`app/README.md`](file:///C:/Users/Sudo/antigravity/GAMMA/app) for details.

---

### 💻 2. Windows Desktop
- **Prerequisites**: Node.js 18+ (tested on Node.js 24), npm.
- **Setup & Run**:
  ```powershell
  cd windows
  npm install
  npm run build:vite
  npm start
  ```
- See [`windows/README.md`](file:///C:/Users/Sudo/antigravity/GAMMA/windows/README.md) for details.

---

### 🍎 3. iOS (iPhone & iPad)
- **Prerequisites**: macOS, Xcode 14.0+ (iOS 16.0+ deployment target).
- **Run in Xcode**:
  ```bash
  open ios/GamaMusic.xcodeproj
  ```
- Supports background playback mode (`UIBackgroundModes: audio`) and full Control Center / Dynamic Island / Lock Screen controls via `MPNowPlayingInfoCenter`.
- See [`ios/README.md`](file:///C:/Users/Sudo/antigravity/GAMMA/ios/README.md) for details.

---

## 👤 Lead Architect

**VHUWON MATHERS**  
*Lead Architect & Systems Engineer*  
GitHub: [https://github.com/sudovu](https://github.com/sudovu)  
Project Repository: [https://github.com/sudovu/Project_gama](https://github.com/sudovu/Project_gama)

---

## 📜 Compliance & Attribution
All streaming content is handled through YouTube's official embedded IFrame APIs respecting platform terms of service, content creator licensing, and digital copyright standards.
