# GAMA Release v1.7

**Release Date:** September 2026  
**Build Artifact:** [GAMA-v1.7.apk](./GAMA-v1.7.apk)  
**Package:** com.aistudio.gamma.freq

---

## What's New in v1.7

### 1. Persistent Audio Playback When Video Player is Closed
- **Root Cause Fix**: Eliminated the unmounting of `CompliantYouTubeHost` when the user tapped the red `✕` close button on the video player. Previously, conditional composition caused Compose to trigger `DisposableEffect.onDispose` and destroy the WebView.
- **Single Unconditionally Mounted Host**: Refactored `GammaApp.kt` to maintain exactly ONE persistent `CompliantYouTubeHost` node across all playback states (Fullscreen, Expanded Player, Corner PiP, and Closed/Audio-Only).
- **Zero-Disruption Transition**: When video is closed, the container smoothly transitions off-screen (`Modifier.size(1.dp).alpha(0.001f).zIndex(-100f)`), allowing audio and music playback to continue completely uninterrupted.
- **Interactive Album Art & Reopen Video Badge**: Displayed high-resolution artwork with a prominent `▶ Watch Video` pill badge when the video is minimized/closed. Tapping the badge restores the video player immediately in sync with playback.

### 2. Custom Duration Sleep Timer with Fade-Out
- **Custom Duration Bottom Sheet Option**: Added a dedicated "Custom Duration... Choose any time from 1 to 180 minutes" action to the Sleep Timer bottom sheet.
- **Precision Slider & Stepper Dialog**: Designed an interactive dialog featuring:
  - Real-time minute readout (e.g. `35 min`).
  - Interactive turquoise slider spanning 1 to 180 minutes.
  - One-tap quick adjustment steppers (`-5m`, `+5m`, `+15m`).
  - Gentle 30-second volume fade-out before audio pausing.
- **Clean Top Header**: Removed the redundant moon sleep icon from the top-right header of `GammaPlayerScreen`, keeping only the track specification Info (`ℹ`) button. Active sleep timers now cleanly display in the top pill badge and bottom action chip with real-time countdown.

### 3. Seamless In-App Spotify & Google Taste Synchronization
- **In-App Taste Management**: Replaced external browser OAuth redirects (which navigated away to Chrome) with native in-app Connection & Taste Customization dialogs for both Spotify and Google / YouTube Music.
- **Interactive Resonance Genre Chips**: Added editable profile handles and selectable genre/resonance chips (e.g., *Alternative Rock*, *Nu-Metal*, *Post-Grunge*, *Melodic Metal*, *Heavy Rap*, *Cyberpunk EDM*, *Synthwave*, *Lo-Fi*, *Ambient*, *Indie Folk*).
- **Live Feed Synchronization**:
  - Wired `TastePreferenceManager.syncLiveTastes(youtubeProvider)` to dynamically query YouTube Music for user-selected genres while filtering out long compilation mixes with `SmartQueueEngine`.
  - Populated live synced single tracks into the Discover feed's "Spotify Sync" and "YouTube Music" shelves.

---

## Installation

```bash
adb install -r versions/v1.7/GAMA-v1.7.apk
```
