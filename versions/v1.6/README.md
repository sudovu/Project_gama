# GAMA Release v1.6

**Release Date:** September 2026  
**Build Artifact:** [GAMA-v1.6.apk](./GAMA-v1.6.apk)  
**Package:** com.aistudio.gamma.freq

---

## What's New in v1.6

### 1. Rock-Solid Seekbar Time Display (Zero Flickering)
- **Eliminated Duration Race Conditions**: In YouTube playback mode, resolved conflicting duration sources between the fallback metadata duration and real-time IFrame bridge duration.
- **Persistent Engine Duration**: Added `@Volatile private var externalDurationMs` in `GamaAudioEngine` and preserved real-time video duration in `PlaybackManager.startProgressTicker()`.
- **Accurate Millisecond Countdown**: Removed float roundoff calculation jitters, making elapsed and remaining time (`-m:ss` / `-h:mm:ss`) count down synchronously with zero second jumping.
- **Monospace Text Stabilization**: Formatted timestamp indicators with `FontFamily.Monospace` and fixed minimum width boundaries (`Modifier.widthIn(min = 52.dp)`), completely preventing digit jitter and layout shift.
- **Seekbar Zero Reset**: Guaranteed seekbar and counter immediately reset to `0:00` upon skipping tracks or natural song transitions.

### 2. Pure Single Song Queuing & Smart Suggestions
- **Automated Collection & Mix Filtering (`SmartQueueEngine`)**:
  - Implemented `isCollectionOrMix(title, durationSeconds)` detection heuristics.
  - Automatically identifies and filters out 1-to-2-hour compilations, full albums, mixtapes, and user-compiled playlists (detects `"mix"`, `"collection"`, `"full album"`, `"playlist"`, `"discography"`, etc., or tracks > 8 minutes).
- **Single-Focused YouTube Queries**:
  - Updated genre queries to explicitly target individual official singles (`"top hip hop rap hits single official music video"`, `"best rock metal songs single official music video"`, etc.).
- **Smart Queue Auto-Advance**:
  - When clicking "Next" (`skipNext`) or when a song finishes, GAMA automatically selects or discovers high-quality single songs matching the active genre.
  - Replaced bulk albums with hit single songs (e.g. *Slipknot - Psychosocial*, *Bullet For My Valentine - Tears Don't Fall*, *System Of A Down - Toxicity*, *Disturbed - Down With The Sickness*, *KoRn - Blind*).

### 3. Unlimited Genre Radio Experience
- **Removed Song Count Restrictions**: Cleaned up the genre header to display `"UNLIMITED STREAM"` rather than fixed song count badges.
- **Infinite Genre Discovery**: Endless radio playback that continuously fetches new related single tracks from YouTube as the user listens.

---

## Installation

```bash
adb install -r versions/v1.6/GAMA-v1.6.apk
```
