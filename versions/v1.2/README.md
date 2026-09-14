# GAMA Release v1.2

**Release Date:** September 2026  
**Build Artifact:** [`GAMA-v1.2.apk`](./GAMA-v1.2.apk)  
**Package:** `com.aistudio.gamma.freq`

---

## What's New in v1.2

### 1. Relocated "Developed By / About the Creator" Section to the Bottom
- Relocated the developer card (`ABOUT THE CREATOR` / `DEVELOPED BY: Vhuwon Mathers @sudovu`) from the very top of the About & Developer tab to the bottom of the screen (placed after `Provider Compliance & Attribution`).
- Settings, Audio Specifications, Themes, Equalizer DSP, and Connected Accounts are now instantly accessible at the top of the tab without scrolling past creator cards.

### 2. Restored and Upgraded 5-Band Real-Time Hardware Equalizer
- Fixed non-responsive Equalizer controls across both the Player Screen and Profile Settings Screen.
- Connected Android hardware audio effects directly via priority-level control (`AndroidHardwareEqualizer` priority 1000, `BassBoost`, `Virtualizer`, and `LoudnessEnhancer`).
- Added smart center frequency band mapping (60 Hz, 230 Hz, 910 Hz, 3.6 kHz, 14 kHz).
- Integrated hardware effects with Web Audio API BiquadFilter equalizer pipeline in the YouTube WebView bridge (`window.setGammaEqualizer`).
- Interactive preset chips (`Balanced`, `Bass Boost`, `Vocal Focus`, `Synthwave`, `Electronic`), real-time frequency shaping sliders with instant dB readouts, and master bypass switch.
- Added working **Spatial Zero-Phase Matrix** toggle hooked directly to hardware stereo widener (`Virtualizer`).

---

## Installation

```bash
adb install -r versions/v1.2/GAMA-v1.2.apk
```
