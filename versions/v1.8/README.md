# GAMA Release v1.8

**Release Date:** September 2026  
**Build Artifact:** [GAMA-v1.8.apk](./GAMA-v1.8.apk)  
**Package:** com.aistudio.gamma.freq

---

## What's New in v1.8

### 1. Pull-to-Refresh on Discover & YouTube Streams
- **Native Material 3 PullToRefreshBox**: Wrapped the primary Discover feed within `@OptIn(ExperimentalMaterial3Api)` `PullToRefreshBox` with `rememberPullToRefreshState()`.
- **Cosmic Cyan Visual Indicator**: Styled with `containerColor = GammaSurfaceElevated` and `color = GammaPrimary` for seamless visual harmony with the obsidian/neon cyberpunk palette.
- **Dynamic Stream Invalidation & Reload**:
  - Wired `viewModel.refresh(tasteManager, youtubeProvider)` to re-fetch dynamic quick picks, clear loaded genre caches, and force real-time taste profile synchronization.
  - Automatically loads brand-new single tracks and refreshed YouTube recommendation shelves upon swiping down.

### 2. Full Android 11+ (API 30+) Package Visibility & Intent Queries
- **`<queries>` Manifest Declaration**: Fixed package visibility restrictions that caused external links to fail silently on modern Android versions. Added intent filters and package queries for:
  - `com.google.android.gm` and `mailto:` (Gmail & Email Clients)
  - `com.spotify.music` and `spotify:` (Spotify Native App & URI schemes)
  - `com.google.android.apps.youtube.music` and `https:` (YouTube Music & Browsers)
- **Robust Multi-Tier Launch Strategy**:
  - **Email**: Tries native Gmail app first (`com.google.android.gm`), falls back to standard email client chooser (`ACTION_SENDTO`), then browser Gmail (`mail.google.com/mail/?view=cm...`), while copying the developer address to clipboard with a confirmatory Toast.
  - **Spotify**: Tries native Spotify app package, falls back to `spotify:user:...` / `spotify:home` URI, and gracefully degrades to `open.spotify.com` web player.
  - **YouTube Music**: Tries official app package (`com.google.android.apps.youtube.music`) before falling back to `music.youtube.com`.

### 3. Quick "Open App" Actions in Integration Dialogs
- Added prominent "Open App" buttons directly inside the Spotify and Google account configuration dialogs, allowing users to verify their linked accounts or jump directly into Spotify and YouTube Music without leaving the settings workflow.

---

## Installation

```bash
adb install -r versions/v1.8/GAMA-v1.8.apk
```
