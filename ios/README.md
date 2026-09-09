# GAMA - Cybernetic Music Platform (iOS Edition)

A native iOS application engineered with **SwiftUI**, **AVAudioSession**, **MediaPlayer (MPNowPlayingInfoCenter)**, and **WKWebView (YouTube IFrame API)**. Designed to deliver low-latency, frequency-calibrated audio playback, lock screen media controls, and background execution.

---

## ⚡ Key Features

- **SwiftUI Native Cybernetic Interface**: Engineered with GAMA's cyber aesthetic (Deep Space `#0a0a0f`, Neon Cyan `#00ffcc`, Cyber Violet `#a855f7`, and Neon Pink `#ec4899`).
- **Background Playback Architecture**: Configured with `AVAudioSessionCategoryPlayback` and `UIBackgroundModes = ["audio"]` to guarantee audio playback when screen is locked or while multitasking in other apps.
- **Lock Screen & Dynamic Island Media Controls**: Fully hooked into `MPNowPlayingInfoCenter` and `MPRemoteCommandCenter` (play, pause, skip forward/backward, scrub position, and dynamic album artwork).
- **5-Band Hardware DSP Equalizer**: 60Hz, 230Hz, 910Hz, 3.6kHz, and 14kHz audio frequency shaping with custom presets (*Flat*, *Bass Boost*, *Cyber Rock*, *Vocal Clarity*, *Electronic*) and 432Hz Harmonic Resonance switch.
- **Embedded Audio/Video Engine**: Seamless `WKWebView` YouTube IFrame playback engine with origin protections and bidirectional Swift JavaScript bridge.
- **Offline Vault & Favorites**: Save tracks, store download states, and create offline quick-play collections.
- **Developer Profile**: Dedicated Architect credential card for **VHUWON MATHERS** (*Lead Architect & Systems Engineer*).

---

## 🛠️ Prerequisites

- **macOS**: macOS Monterey (12.0+) / macOS Ventura / macOS Sonoma
- **Xcode**: Xcode 14.0 or higher (compatible with Xcode 15 / 16)
- **iOS Target**: iOS 16.0 or higher (iPhone & iPad)
- **Swift**: Swift 5.0+

---

## 🚀 Building & Installing

### 💻 Installing from a Windows Laptop (No Mac Required)
If you are developing on a Windows machine, you do **not** need a Mac:
1. GitHub Actions automatically builds the `.ipa` using cloud macOS runners on every push.
2. Download `GAMA-v1.0-iOS-IPA` from the repository's [GitHub Actions](https://github.com/sudovu/Project_gama/actions) tab.
3. Use **Sideloadly for Windows** to sign and install the app directly onto your iPhone over USB using any standard free Apple ID!
4. 👉 Full detailed step-by-step instructions: [Windows to iOS Installation Guide](WINDOWS_INSTALL_GUIDE.md).

---

### 🍏 Installing from macOS / Xcode
1. Open the project in Xcode:
```bash
open ios/GamaMusic.xcodeproj
```
2. In the top toolbar, select the `GamaMusic` scheme and your connected iPhone.
3. Under **Signing & Capabilities**, select your Apple ID.
4. Press `Cmd + R` (Play) to run and install directly to your device.

---

## 📁 Architecture Overview

```
ios/
├── GamaMusic.xcodeproj/
│   └── project.pbxproj               # Xcode project configuration & build targets
└── GamaMusic/
    ├── GamaMusicApp.swift            # SwiftUI App entry point
    ├── Models/
    │   └── Models.swift              # GamaTrack, GamaGenre, EqualizerPreset models & 20 tracks
    ├── Audio/
    │   ├── GamaAudioSession.swift    # AVAudioSession background playback management
    │   ├── NowPlayingManager.swift   # MPNowPlayingInfoCenter & MPRemoteCommandCenter bridge
    │   ├── YouTubePlayerView.swift   # WKWebView YouTube IFrame audio/video engine
    │   └── AudioManager.swift        # Central reactive state coordinator (@MainActor)
    ├── Views/
    │   ├── MainTabView.swift         # Root navigation with docked mini-player
    │   ├── DiscoverView.swift        # Featured harmonic banner, genres, and track cards
    │   ├── SearchView.swift          # Instant real-time frequency and track scanner
    │   ├── LibraryView.swift         # Vault for favorites, downloaded tracks, and 432Hz core
    │   ├── EqualizerView.swift       # 5-Band DSP equalizer with vertical sliders & presets
    │   ├── ProfileView.swift         # VHUWON MATHERS developer card and ecosystem overview
    │   ├── PlayerView.swift          # Full-screen glowing player with seekbar and PiP toggle
    │   └── MiniPlayerView.swift      # Bottom persistent player bar
    └── Resources/
        ├── Info.plist                # Background modes (audio), transport security, permissions
        └── img_vhuwon_profile.jpg    # Developer profile photograph
```

---

## 👤 Developer

**VHUWON MATHERS**  
*Lead Architect & Systems Engineer*  
GitHub: [https://github.com/sudovu](https://github.com/sudovu)  
Project Repository: [https://github.com/sudovu/Project_gama](https://github.com/sudovu/Project_gama)
