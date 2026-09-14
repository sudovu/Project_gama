# GAMA Release v1.4

**Release Date:** September 2026  
**Build Artifact:** [GAMA-v1.4.apk](./GAMA-v1.4.apk)  
**Package:** com.aistudio.gamma.freq

---

## What's New in v1.4

### 1. Harmonic Curved Waveform Seekbar (`GammaCurvedWaveformSeekbar`)
- **46-Bar Dynamic Sinusoidal Spectrum**: Replaced the generic flat Android slider with a custom high-performance Compose Canvas visualizer featuring 46 curved harmonic audio bars with dynamic sinusoidal envelopes.
- **Audio Visualizer Modulation**: Live audio breathing oscillation driven by `visualizerBands` when playing.
- **Glowing Cyber Scrubber**: Cybernetic thumb indicator featuring dual-ring cyan glow aura with animated scaling on scrub touch.
- **Scrubbing Timestamp Tooltip**: Floating high-contrast tooltip bubble (`m:ss`) displaying exact scrub location while sliding.
- **Accurate Scrub & Tap-to-Seek**: Instantly jumps to exact track positions on touch tap or scrub drag with haptic vibration feedback.

### 2. Precision Sleep Timer Engine with Gentle 30s Audio Ducking
- **Comprehensive Duration Presets**: Quick options for **15 Minutes**, **30 Minutes**, **45 Minutes**, **60 Minutes**, and **End of Current Track**.
- **Real-Time Countdown Badging**: Live status display showing remaining time (`MM:SS`) in the top app bar glowing badge pill, top-right moon button, and dedicated bottom action chip (`🌙 14:59`).
- **Gentle 30-Second Audio Fade-Out**: In the final 30 seconds before pausing, GAMA smoothly and logarithmically ducks audio volume across both native media and YouTube audio streams to prevent abrupt interruptions when falling asleep.
- **Dedicated Management Modal**: Interactive bottom sheet with active countdown card, `+15 Mins` quick-extend button, and instant cancel option.

### 3. Gesture Engine (`GammaGestureOverlay`)
- **Double-Tap 5-Second Skip**:
  - Double-tap left side of the artwork/video container: Skips backward 5 seconds with animated `⟲ -5s` HUD ripple badge.
  - Double-tap right side: Skips forward 5 seconds with animated `+5s ⟳` HUD ripple badge.
- **Hold to Continuous Seek / 2x Speed**:
  - Long-press hold left side: Continuous smooth rewinding (`⏪ REWINDING...` HUD badge).
  - Long-press hold right side: Instant 2.0x playback speed acceleration (`⚡ 2X SPEED` glowing HUD badge) with automatic reset upon release.

### 4. Professional Variable Playback Speed Control
- **Variable Speed Selector**: Dedicated speed selector sheet with standard audiophile presets: `0.5x`, `0.75x`, `0.9x`, `1.0x` (normal), `1.25x`, `1.5x`, and `2.0x`.
- **Hardware & Web Synchronization**: Applies speed adjustments to both Android's native `MediaPlayer` (`PlaybackParams`) and YouTube IFrame video player (`setPlaybackRate`).
- **Real-Time Speed Chip**: Bottom quick-action chip dynamically displays the active speed multiplier (e.g., `⏱️ 1.5x`).

### 5. Advanced Queue Reordering & Organization
- **Visual Up Next Separation**: Clear division between currently playing track and the upcoming queue.
- **Queue Track Reordering**: Up/down reorder buttons on queue items to instantly adjust listening priority.
- **One-Tap Clear Queue**: Clean slate action to quickly clear remaining tracks.

---

## Installation

```bash
adb install -r versions/v1.4/GAMA-v1.4.apk
```
