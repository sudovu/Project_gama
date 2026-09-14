# GAMA Music - Release v1.1

**Build Date**: September 2026  
**Platform**: Android, Windows, iOS  
**Package / App ID**: `com.aistudio.gamma.freq`  
**Version**: `1.1` (Build 2)  

---

## What's New in v1.1

### 1. Compact & Discreet Video Overlay Controls
- Resized and streamlined all video player overlay buttons (Close `✕`, Fullscreen `⛶`, Minimize `🗕`):
  - **Fullscreen Video**: Scaled down to 26dp frame with 14dp icons for maximum immersion.
  - **Corner PiP Video**: Scaled down to 22dp frame with 12dp icons, reducing screen obstruction.
  - Safe-area status bar insets ensure controls never collide with camera notches or system status bars.

### 2. Pure Music Mode on Close (Seamless YouTube Audio Persistence)
- Tapping the Close button (`✕`) on a playing song's video switches immediately to **Pure Music Mode**:
  - Audio continues playing smoothly without pausing or restarting.
  - Background audio engine keeps the stream active via an offscreen compliant WebView host (`alpha(0.001f)` 1x1 Box).
  - The expanded player displays full high-resolution artwork, ambient glowing aura, and a real-time pulsating frequency equalizer.
  - A subtle `[▶ Watch Video]` badge in the player allows re-opening the video overlay at any time with a single tap.

### 3. Persistent Music Playback on Track Changes
- Once the video is closed, skipping to the next song (`Next` / `Previous`) **will not pop up the video again**.
- The player respects your audio-only preference for the remainder of your listening session.

### 4. Google & Spotify Account Linking & Taste Sync
- **Profile Screen Integration**:
  - Connected Accounts section featuring **Spotify** and **Google / YouTube Music**.
  - One-tap account linking/unlinking state management.
  - Interactive "Sync Taste Profile" button analyzing listening history.
  - Generates taste tags: `Electronic`, `Synthwave`, `Cyberpunk`, `Retrowave`, `Ambient`, `Darkwave`, `Lo-Fi`, `EDM`.
- **TastePreferenceManager**:
  - Centralized preference manager persisting connected account states and taste vectors.

### 5. Dynamic Algorithmic Feed in Discover
- **Discover Screen** dynamically updates based on linked platforms:
  - **Spotify Sync (Taste Profile Matches)**: Displays tailored tracks matching your Spotify genre affinity.
  - **YouTube Music (Resonances For You)**: Suggests trending and deep-cut tracks matching your Google/YouTube preferences.

### 6. Multi-Platform Playlist Importer
- Built-in universal playlist importer dialog in the **Library** screen:
  - Supports importing playlists via URL from **Spotify**, **YouTube Music**, and **Apple Music**.
  - Includes curated Quick Presets (*Cyberpunk 2077 Night Drive*, *Synthwave Chillout*, *Lo-Fi Beats for Coding*).
  - Automatically saves imported playlists and tracks directly into Room database for offline access.

### 7. Immersive Fullscreen & Navigation Bar Behavior
- True edge-to-edge fullscreen hides system navigation bars during video playback.
- System navigation bars are strictly visible only when the player is minimized or PiP is active.

### 8. Multi-Platform Parity
- **Windows Desktop (`App.jsx`)**:
  - Minimized button dimensions (`w-3 h-3`), close button seamlessly shifts into pure music mode without stopping playback.
- **iOS (`PlayerView.swift`)**:
  - Refined button sizes (16pt), closing PiP maintains background audio session uninterrupted.

---

## Artifacts Included

- `GAMA-v1.1.apk` (~25.0 MB) - Android release package for universal architectures (`arm64-v8a`, `armeabi-v7a`, `x86_64`).
