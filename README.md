# GAMA — Cybernetic High-Fidelity Audio & Video Platform

[![Android](https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-brightgreen.svg)](https://developer.android.com)
[![Windows](https://img.shields.io/badge/Windows-10%20%2F%2011%20Desktop-0078D6.svg)](https://www.microsoft.com/windows)
[![iOS](https://img.shields.io/badge/iOS-16.0%2B%20SwiftUI-000000.svg)](https://developer.apple.com/ios/)
[![Audio Engine](https://img.shields.io/badge/DSP-16--Band%20FFT%20%7C%20Parametric%20EQ%20%7C%20ReplayGain-cyan.svg)]()
[![LAN Sync](https://img.shields.io/badge/Sync-mDNS%20%7C%20WebSocket%20%7C%20HTTP%20Range%20206-purple.svg)]()
[![Architect](https://img.shields.io/badge/Architect-VHUWON%20MATHERS-blueviolet.svg)](https://github.com/sudovu)

**GAMA** is a high-fidelity cybernetic media platform engineered for audiophiles and power users across **Android**, **Windows Desktop**, and **iOS**. GAMA combines studio-grade DSP audio processing, real-time hardware FFT frequency analysis, interactive SoundCloud-style waveform scrubbers, dynamic album-art ambient theming, and a zero-cloud local-network (LAN) sync engine that connects phones, PCs, and web browsers with zero third-party accounts.

Architected and developed by **VHUWON MATHERS**.

---

## ⚡ Core Audiophile & Standout Differentiators

```
┌────────────────────────────────────────────────────────────────────────┐
│                        GAMA AUDIOPHILE ARCHITECTURE                     │
├────────────────────────┬───────────────────────┬───────────────────────┤
│    HARDWARE DSP & FFT  │   SMART UI & SCRUB    │   LAN SYNC & STREAM   │
│  • 16-Band Real-Time   │  • SoundCloud-Style   │  • mDNS Zero-Config   │
│    Frequency Analyzer  │    100-Peak Waveform  │  • 4-Digit PIN Auth   │
│  • Preamp Gain & EQ    │  • Dynamic Ambient    │  • Lossless HTTP 206  │
│  • ReplayGain Normal.  │    Album Theming      │    Range Streaming    │
│  • Gapless Crossfade   │  • Smart Shelf Chips  │  • PC-to-Phone Push   │
│  • Lookahead Buffering │  • Audio / Video PiP  │  • Browser Web Remote │
└────────────────────────┴───────────────────────┴───────────────────────┘
```

### 1. Studio-Grade Audio Engine & Hardware FFT
- **16-Band Real-Time Spectrum Analyzer:** Dual-mode hardware `Visualizer` engine that directly captures PCM output at 60fps, paired with procedural multi-band DSP spectrum fallback when `RECORD_AUDIO` is withheld.
- **Audiophile DSP Chain:** Multi-band parametric/graphic EQ with presets, preamp master gain, replay-gain loudness normalization, and configurable gapless crossfades ($0\text{ms}..8000\text{ms}$).
- **Lookahead Pre-Buffering:** Automatically identifies and pre-buffers upcoming queue tracks when remaining playback $\le 10$ seconds for instantaneous, glitch-free transitions.
- **Audio Quality Badges:** Automatic codec and sample-rate inspection dynamically badges tracks (`FLAC • 24-bit/96kHz`, `WAV • 16-bit/44kHz`, `HQ AUDIO • 320 kbps`).

### 2. SoundCloud-Style Interactive Waveform Scrubber
- **Asynchronous Peak Extraction:** Background `MediaExtractor` decoder calculates 100 normalized amplitude peaks with realistic envelope fallback for live streams.
- **High-Performance Canvas Scrubber:** 60fps Jetpack Compose `Canvas` rendering 100 rounded bars with dynamic theme gradients, interactive tap/drag gesture seeking, and a floating timestamp bubble.
- **Dual Seeker Modes:** Instant one-tap toggle between the waveform scrubber and the cybernetic circular arc scrubber.

### 3. Seamless Audio-Video Continuity & Floating PiP
- **One-Tap Audio/Video Toggle:** Switch between full-frame video playback and lightweight background audio-only mode without reloading or losing timestamp position.
- **Floating Mini-Player & PiP:** Continuous playback while browsing catalogs, managing playlists, or multitasking across other applications.

### 4. Smart Library Intelligence & Dynamic Ambient Theming
- **Dynamic Palette Extraction:** Extracts dominant, vibrant, and muted tones from active album art to tint player glows, progress bars, and lyrics containers in real-time.
- **Audiophile Smart Shelves:** Instant filter chips in the library view:
  - `Hi-Res / Lossless` (FLAC, WAV, ALAC, $\ge 24$-bit or $\ge 96\text{kHz}$)
  - `Most Played` (ranked by user play counts)
  - `Recently Added` (new additions within 14 days)
  - `Video Tracks` (continuity tracks with video streams)

### 5. Cross-Device LAN Sync (Android $\leftrightarrow$ Windows Desktop $\leftrightarrow$ Web)
- **Zero Cloud Accounts:** All synchronization runs over your local Wi-Fi network using standard TCP, HTTP, and RFC 6455 WebSockets on port `8765`.
- **mDNS Auto-Discovery (`_gama-sync._tcp`):** Android broadcasts service info via `NsdManager`; Windows desktop discovers phones with 1 click.
- **4-Digit PIN Security:** Cryptographic session tokens (`POST /pair`) ensure only authorized devices on your network can control playback or stream audio.
- **Direct LAN Audio Streaming (`GET /stream`):** Full HTTP Range-request support (`Accept-Ranges: bytes`, `206 Partial Content`) lets your PC speakers play lossless audio streamed directly from your phone.
- **Two-Way Queue Push (`POST /queue/push`):** Send tracks or upload local `.flac`/`.mp3` files from PC straight to your phone's active playback queue with chunked disk streaming.
- **Mobile/Browser Web Remote (`GET /`):** Point any browser (iPhone Safari, iPad, Android Chrome, Mac/Linux) to `http://<phone_ip>:8765/` to control playback, view live FFT bars, or stream audio without installing an app.

---

## 🌐 Tri-Platform Ecosystem Structure

```
Project_gama/
├── app/                      # Android Native (Kotlin 2.0 + Jetpack Compose + Room + ExoPlayer)
│   ├── src/main/java/com/example/
│   │   ├── core/media/       # Audio Engine, DSP, Equalizer, PlaybackService
│   │   ├── core/remote/      # GamaLanSyncServer (mDNS, WebSockets, HTTP 206)
│   │   ├── core/ui/          # Waveform Scrubber, MiniPlayer, Visualizers
│   │   └── feature/player/   # GammaPlayerScreen, Track Specs Sheet, Lyrics
├── windows/                  # Windows Desktop (Electron 31 + React 18 + Vite 5 + Tailwind)
│   ├── src/main/             # Main process, System Tray, Media Keys, Subnet Scanner
│   ├── src/preload/          # Safe ContextBridge IPC
│   └── src/renderer/         # Cybernetic Player UI, LAN Remote Client, Web Audio
├── ios/                      # iOS Native Application (SwiftUI + AVFoundation + MediaPlayer)
├── .github/workflows/        # Automated Multi-Platform CI/CD
│   ├── release.yml           # Automated GitHub Releases (Android APK + Windows EXE)
│   └── build-platforms.yml   # Multi-platform build verification
└── README.md
```

---

## 🚀 Building & Packaging

### 📱 Android Application

1. **Prerequisites:** JDK 17 (or 21), Android SDK Platform 34.
2. **Run Unit Tests:**
   ```bash
   ./gradlew testDebugUnitTest
   ```
3. **Build Signed Release APK:**
   ```bash
   ./gradlew assembleRelease
   ```
   Output: `app/build/outputs/apk/release/app-release.apk`
4. **Install to Device via ADB:**
   ```bash
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```

---

### 💻 Windows Desktop Application

1. **Prerequisites:** Node.js 20+, npm.
2. **Install Dependencies:**
   ```powershell
   cd windows
   npm ci
   ```
3. **Run in Development:**
   ```powershell
   npm run dev
   ```
4. **Build Production Web Bundle:**
   ```powershell
   npm run build
   ```
5. **Package Standalone Windows Executables:**
   ```powershell
   npm run dist
   ```
   Outputs in `windows/release/`:
   - `GAMA Setup 1.1.0.exe` (NSIS Installer with Desktop & Start Menu shortcuts)
   - `GAMA 1.1.0.exe` (Portable x64 standalone executable)

---

### 🍎 iOS Application

- **Automated Cloud Builds:** GitHub Actions packages `GAMA-v1.0.ipa` on macOS runners automatically.
- **Sideloading:** Install via Sideloadly or AltStore using any free Apple ID.
- **Local Xcode Build:** Open `ios/GamaMusic.xcodeproj` and press `Cmd + R`.

---

## 🔄 GitHub Actions CI/CD Release Pipeline

GAMA includes a continuous delivery pipeline in [`.github/workflows/release.yml`](.github/workflows/release.yml).

- **Triggers:** Automatically on version tags (e.g. `git tag v1.2.0 && git push origin v1.2.0`) or manually via GitHub Actions **Run workflow** button.
- **Matrix Build:**
  - **Android:** Compiles, runs unit tests, and signs release APKs on Ubuntu.
  - **Windows:** Compiles React bundle and packages NSIS installer and portable `.exe` on `windows-latest`.
- **Automated Publishing:** Bundles all binaries into a GitHub Release with auto-generated release notes.

---

## 👤 Lead Architect

**VHUWON MATHERS**  
*Lead Architect & Systems Engineer*  
- GitHub: [@sudovu](https://github.com/sudovu)  
- Repository: [sudovu/Project_gama](https://github.com/sudovu/Project_gama)

---

## 📜 Compliance & Attribution
All streaming content is handled through YouTube's official embedded IFrame APIs respecting platform terms of service, content creator licensing, and digital copyright standards.
