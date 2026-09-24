# GAMA v1.9 — Dynamic Rotating Suggestions, Broadened Search & Seamless Account Linking

**Release Date:** September 2026  
**Build:** 19 (`versionCode = 19`, `versionName = "1.9"`)  
**Package:** `com.example.gamma`  
**Binary File:** `GAMA-v1.9.apk`

---

## What's New in v1.9

1. **Pull-to-Refresh Rotating Suggestions (YouTube Style)**:
   - Discover feed dynamically picks fresh seed queries on every pull-down-to-refresh across Trending, Hip-Hop, Rock & Metal, Pop Hits, and Classics.
   - Quick Picks and suggestions dynamically rotate to new authentic single tracks instead of repeating cached seeds.

2. **Official YouTube App Launch**:
   - Upgraded YouTube integration launcher with multi-tier fallback: official YouTube app (`com.google.android.youtube`) -> YouTube Music (`com.google.android.apps.youtube.music`) -> `vnd.youtube:` URI -> web fallback.
   - Declared packages and URI schemes in `<queries>` for Android 11+ compatibility.

3. **Streamlined Account Linking via Email ID**:
   - In-app connection dialogs specifically accept Spotify and Google / YouTube account email IDs with direct email keyboard optimization.
   - Linked email IDs are cleanly reflected on profile cards and adapt Discover recommendations seamlessly.

4. **Cleaner, Broader Search Results**:
   - Removed all `YT MUSIC` / `YOUTUBE` pill badges and stripped "YouTube Music" / "YouTube Audio" genre suffixes.
   - Broadened search query execution with multiple query expansions, returning 25–35+ single tracks.

5. **Developer Profile Refinement**:
   - Developer avatar resized from 116dp down to a sleek 64dp with glowing neon ring border.
   - Completely removed developer phone number across all files, resources, and copy clipboards; replaced with Portfolio & Release Archive card linking to `https://gautambhuwan.com.np/projects.html#gamma-releases`.

6. **Full Version & Binary Synchronization**:
   - Synchronized `build.gradle.kts` (`versionCode = 19`, `versionName = "1.9"`), internal UI (`Version 1.9.0 (Build 19)`), and binary package name (`GAMA-v1.9.apk`).
