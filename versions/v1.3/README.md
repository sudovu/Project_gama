# GAMA Release v1.3

**Release Date:** September 2026  
**Build Artifact:** [GAMA-v1.3.apk](./GAMA-v1.3.apk)  
**Package:** com.aistudio.gamma.freq

---

## What's New in v1.3

### 1. Active External Linking for Spotify & Google / YouTube Music
- **Direct Spotify Account Authorization & Sign-In**: Tapping **Link Spotify Account** directly launches an external intent opening Spotify (https://accounts.spotify.com/en/login?continue=https%3A%2F%2Fopen.spotify.com or Spotify app) so users can immediately log in and authenticate their account.
- **Direct Google Account Chooser for YouTube Music**: Tapping **Link Google Account** directly opens Google's native Account Chooser (https://accounts.google.com/AccountChooser?service=youtube&continue=https%3A%2F%2Fmusic.youtube.com%2F or YouTube Music app) to authenticate and link the user's Google credentials.
- **Real-Time Taste Synchronization**: Automatically syncs top genres, frequency resonances, and listening tastes directly into GAMA's Discover feed, dynamically rendering taste-matched playlists and songs (e.g. Rock & Metal, Alternative Rock, Nu Metal, Synthwave).

### 2. Enhanced Connected Account Controls & Profile Dialogs
- **Open App Action**: Connected cards now feature a direct Open App button to jump straight into Spotify or YouTube Music with a single tap.
- **Sync Taste Quick-Action**: Instantly re-synchronizes taste profiles and refreshes algorithmic feed recommendations.
- **In-App Profile Edit Dialogs**: Tapping the edit icon next to the connected username or Google email opens an in-app customization dialog to modify credentials or usernames without unlinking.
- **Seamless Unlinking**: Allows one-tap unlinking to clear local credentials and taste profile references safely.

---

## Installation

`ash
adb install -r versions/v1.3/GAMA-v1.3.apk
`
