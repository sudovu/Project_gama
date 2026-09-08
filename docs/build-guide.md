# GAMMA — Build Guide

## Current State

GAMMA is currently a **web application** built with React + Vite + Tailwind CSS. To create an Android APK, you have two options:

---

## Option 1: Capacitor (Fastest — Wrap Web App)

Convert the existing web app into a native Android APK using Capacitor.

### Steps

```bash
# 1. Install Capacitor
npm install @capacitor/core @capacitor/cli
npm install @capacitor/android

# 2. Initialize Capacitor
npx cap init GAMMA com.example.gamma --web-dir=dist

# 3. Build the web app
npm run build

# 4. Add Android platform
npx cap add android

# 5. Copy web assets to Android project
npx cap sync

# 6. Open in Android Studio
npx cap open android
```

### In Android Studio

1. Wait for Gradle sync to complete
2. Connect a device or start an emulator
3. Click **Run** (▶️) to test
4. For APK: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
5. APK will be in `android/app/build/outputs/apk/debug/`

### Required Android Studio Setup

- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34
- JDK 17
- Android Build Tools 34.0.0

---

## Option 2: Native Android (Full Rewrite)

Rewrite GAMMA as a native Android app using Kotlin + Jetpack Compose.

### Project Structure

```
app/
├── src/main/
│   ├── java/com/example/gamma/
│   │   ├── MainActivity.kt
│   │   ├── GammaApplication.kt
│   │   ├── di/                    # Dependency Injection
│   │   ├── data/
│   │   │   ├── local/             # Room Database
│   │   │   ├── remote/            # API calls
│   │   │   └── repository/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── usecase/
│   │   ├── ui/
│   │   │   ├── theme/
│   │   │   ├── components/
│   │   │   ├── discover/
│   │   │   ├── search/
│   │   │   ├── library/
│   │   │   ├── player/
│   │   │   └── profile/
│   │   └── provider/
│   │       ├── MusicProvider.kt
│   │       └── YouTubeProvider.kt
│   └── res/
│       ├── values/
│       ├── drawable/
│       └── mipmap/
├── build.gradle.kts
└── proguard-rules.pro
```

### Key Dependencies

```kotlin
// build.gradle.kts
dependencies {
    // Compose
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Architecture
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Database
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Media
    implementation("androidx.media3:media3-exoplayer:1.2.1")
}
```

---

## YouTube Integration

### Required Setup

1. **YouTube Data API v3 Key**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create project → Enable YouTube Data API v3
   - Create API key → Restrict to your app
   - Copy to `.env`: `YOUTUBE_API_KEY=your_key`

2. **API Quotas**
   - Free tier: 10,000 units/day
   - Search: 100 units/request
   - Video details: 1 unit/request

3. **Compliance Requirements**
   - ✅ Use official YouTube Data API v3
   - ✅ Use YouTube IFrame Player API for playback
   - ❌ Cannot extract stream URLs
   - ❌ Cannot bypass ads
   - ❌ Cannot download content
   - ❌ Cannot cache media

### Integration Architecture

```
SearchPage → YouTubeProvider → YouTube Data API v3
                                    ↓
                              Domain Models
                                    ↓
                              UI Components
                                    ↓
                         YouTube IFrame Player (playback)
```

### Current Implementation

The app already includes:
- `YouTubeProvider` class with full API integration
- Provider abstraction (`MusicProvider` interface)
- Mock data for offline development
- Compliance documentation

### To Enable YouTube Search

1. Get API key from Google Cloud Console
2. Set environment variable: `VITE_YOUTUBE_API_KEY=your_key`
3. Update the search page to use `YouTubeProvider`
4. Implement IFrame player for playback

---

## Build Commands

### Web App
```bash
npm install          # Install dependencies
npm run dev          # Development server
npm run build        # Production build
npm run preview      # Preview production build
```

### Android (Capacitor)
```bash
npm run build        # Build web assets
npx cap sync         # Sync to Android
npx cap open android # Open in Android Studio
```

---

## Known Limitations

| Feature | Status | Reason |
|---------|--------|--------|
| YouTube Search | ⚠️ Needs API Key | Requires YouTube Data API v3 key |
| YouTube Playback | ⚠️ Needs IFrame | Must use official IFrame player |
| Background Play | ❌ Not Available | Requires YouTube Premium |
| Offline Playback | ❌ Not Permitted | YouTube ToS restriction |
| Download | ❌ Not Permitted | YouTube ToS restriction |
| Ad-free | ❌ Not Permitted | Requires YouTube Premium |

---

## Next Steps

1. **For quick APK**: Use Capacitor (Option 1)
2. **For full Android experience**: Native rewrite (Option 2)
3. **For YouTube integration**: Get API key and configure
4. **For production**: Set up backend proxy for API security
