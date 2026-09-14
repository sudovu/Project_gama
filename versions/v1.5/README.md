# GAMA Release v1.5

**Release Date:** September 2026  
**Build Artifact:** [GAMA-v1.5.apk](./GAMA-v1.5.apk)  
**Package:** com.aistudio.gamma.freq

---

## What's New in v1.5

### 1. Continuous Curve-Only Seekbar (`GammaCurvedWaveformSeekbar`)
- **Seamless Aerodynamic Arc Track**: Completely replaced discrete vertical wavy bars with a continuous, elegant quadratic bezier curved arc track ($y(t) = y_{\text{base}} - 4 \cdot \text{arcHeight} \cdot t(1 - t)$).
- **Dual-Layer Glow & Dynamic Gradient**:
  - Soft ambient cyan glow backdrop stroke with blurred alpha.
  - Vibrant horizontal gradient played stroke (`ElectricCyan` to `NeonPurple`).
  - Sleek dark obsidian unplayed track (`Color(0xFF232136)`).
- **Luminous Cyber Scrubber Thumb**: Multi-layered circular thumb tracking the mathematical curve with an inner cyan core, glowing outer halo, and breathing touch animation.
- **Silky Smooth Touch Gesture Physics**: Unified pointer gesture handler using `awaitEachGesture` providing:
  - Instant jump-to-position on initial touch down.
  - Continuous 60fps drag scrubbing with smooth visual progression.
  - Clean commit on touch release with haptic tick feedback.
- **Scrubbing Timestamp Tooltip**: High-contrast tooltip pill dynamically floating above the scrubber thumb displaying exact timestamp (`m:ss`).

### 2. Borderless Video Control Bar (`GammaApp.kt`)
- **Circle-Free Minimalist Controls**: Removed circular backgrounds (`CircleShape`) and bright circular border rings around video overlay actions.
- **Clean Vector Icon Buttons**: Close (red `✕`), Maximize / Fullscreen (`⛶`), and Minimize (`⌄`) now float cleanly directly above video frames in both PiP floating overlay and fullscreen viewing modes.
- **Refined Touch Targets & Usability**: Preserved accessible 48dp touch bounds with optimized 18dp/20dp crisp vector iconography.

---

## Installation

```bash
adb install -r versions/v1.5/GAMA-v1.5.apk
```
