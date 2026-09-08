# GAMMA — Your Personal Frequency

A futuristic music discovery and playback platform built with React, TypeScript, Vite, and Tailwind CSS.

## 🎯 Overview

GAMMA is a premium music discovery application that provides users with an immersive, futuristic interface for exploring and listening to music. Inspired by the best qualities of modern music streaming applications, GAMMA has its own unique brand identity, design system, and interaction patterns.

**Core Philosophy:**
> GAMMA is a personal frequency space for discovering music.

## 📱 Building Android APK

GAMMA can be converted to an Android APK using two approaches:

### Option 1: Capacitor (Fastest)
```bash
npm run build
npm install @capacitor/core @capacitor/cli @capacitor/android
npx cap init GAMMA com.example.gamma --web-dir=dist
npx cap add android
npx cap sync
npx cap open android
# Then in Android Studio: Build → Build APK
```

### Option 2: Native Android (Full Rewrite)
See [docs/build-guide.md](docs/build-guide.md) for the complete native Android architecture with Kotlin + Jetpack Compose.

## 🔍 YouTube Integration

GAMMA includes a `YouTubeProvider` with full YouTube Data API v3 integration architecture. To enable real YouTube search:

1. Get a YouTube Data API v3 key from [Google Cloud Console](https://console.cloud.google.com/)
2. Set environment variable: `VITE_YOUTUBE_API_KEY=your_key`
3. See [docs/youtube-integration.md](docs/youtube-integration.md) for full details

**Important:** GAMMA only uses officially supported YouTube APIs and does not circumvent any restrictions.

## ✨ Features

### 🎵 Music Discovery
- **Dynamic Home Feed** — Personalized greeting, mood explorer, trending tracks, new releases
- **Smart Search** — Debounced global search with categorized results and history
- **Curated Playlists** — Hand-tuned frequency collections for every mood
- **Artist & Album Pages** — Rich detail views with discography and popular tracks

### 🎧 Premium Player
- **Immersive Full Player** — Artwork-derived gradients, ambient particles, waveform visualization
- **Mini Player** — Compact playback surface with smooth transitions
- **Queue Management** — Full queue control with reorder, remove, and clear
- **Playback Controls** — Play/pause, next/prev, shuffle, repeat (off/all/one), seek

### 📚 Library
- **Favorites** — Heart tracks to save them locally
- **Recently Played** — Automatic history tracking
- **Playlists** — Create, manage, and play custom playlists
- **Listening Stats** — Weekly activity visualization and genre preferences

### 🎨 Design System
- **Dark-First Theme** — Premium near-black surfaces with violet/cyan accents
- **Light Mode** — Full light theme support
- **GAMMA Brand** — Original typography (Space Grotesk + Inter), custom components
- **Ambient Effects** — Particle systems, gradient backgrounds, smooth animations
- **Accessibility** — ARIA labels, focus management, reduced motion support

## 🏗️ Architecture

```
src/
├── App.tsx                    # Main application shell
├── main.tsx                   # Entry point
├── index.css                  # Global styles & theme
│
├── types/
│   └── index.ts              # TypeScript interfaces
│
├── data/
│   └── mockData.ts           # Mock data (easily replaceable)
│
├── hooks/
│   ├── usePlayer.ts          # Player state management
│   └── useLibrary.ts         # Library state management
│
├── components/
│   ├── SplashScreen.tsx      # Branded splash screen
│   ├── ui/
│   │   ├── GammaArtwork.tsx       # Reusable artwork component
│   │   ├── GammaTrackRow.tsx      # Track list item
│   │   └── GammaSectionHeader.tsx # Section headers
│   └── player/
│       ├── MiniPlayer.tsx         # Compact player
│       ├── FullPlayer.tsx         # Immersive player
│       ├── QueuePanel.tsx         # Queue management
│       ├── WaveformVisualizer.tsx # Audio visualization
│       └── AmbientParticles.tsx   # Background particles
│
└── pages/
    ├── DiscoverPage.tsx      # Home/discover feed
    ├── SearchPage.tsx        # Global search
    ├── LibraryPage.tsx       # User library
    ├── ProfilePage.tsx       # Settings & stats
    ├── PlaylistDetail.tsx    # Playlist view
    ├── ArtistDetail.tsx      # Artist view
    └── AlbumDetail.tsx       # Album view
```

## 🎨 Design Tokens

### Colors
- **Primary:** `#8b5cf6` (Electric Violet)
- **Accent:** `#06b6d4` (Aurora Cyan)
- **Background:** `#050507` (Deep Black)
- **Surface:** `#0d0d12` (Elevated Dark)
- **Text Primary:** `#f8fafc`
- **Text Secondary:** `#94a3b8`

### Typography
- **Display:** Space Grotesk (Headlines, Brand)
- **Body:** Inter (UI text, descriptions)

### Spacing
- Consistent 4px grid system
- Generous padding for premium feel
- Responsive breakpoints for mobile/tablet/desktop

## 🚀 Getting Started

### Prerequisites
- Node.js 18+ 
- npm or yarn

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

### Environment Setup

No environment variables required for the demo version. The application uses mock data that can be easily replaced with a real music API.

## 🎯 Technology Stack

- **React 18** — UI framework
- **TypeScript** — Type safety
- **Vite** — Build tool & dev server
- **Tailwind CSS 4** — Utility-first styling
- **Framer Motion** — Animations & transitions
- **Lucide React** — Icon library
- **Canvas API** — Audio visualization

## 📱 Responsive Design

GAMMA is fully responsive and optimized for:
- 📱 Mobile phones (320px+)
- 📱 Large phones (400px+)
- 📱 Tablets (768px+)
- 💻 Desktop (1024px+)

## ♿ Accessibility

- Semantic HTML structure
- ARIA labels and roles
- Keyboard navigation support
- Focus visible indicators
- Reduced motion support
- Color contrast compliance
- Screen reader friendly

## 🔒 Security & Compliance

### YouTube Integration (Future)
When integrating with YouTube or other music providers:
- ✅ Use only officially supported APIs
- ✅ Implement proper OAuth authentication
- ✅ Never extract stream URLs
- ✅ Never bypass DRM or access controls
- ✅ Never circumvent premium restrictions
- ✅ Never download content without authorization
- ✅ Never cache media without permission
- ✅ Never scrape private endpoints

### Data Privacy
- Local storage for user preferences
- No tracking or analytics in demo version
- No sensitive data collection
- Clear separation of concerns

## 🧪 Testing

```bash
# Type checking
npm run typecheck

# Linting (if configured)
npm run lint
```

## 📦 Build & Deployment

```bash
# Production build
npm run build

# Output directory: dist/
# Deploy dist/ to any static hosting service
```

## 🎨 Customization

### Adding Real Music Data
Replace `src/data/mockData.ts` with API calls to your music provider:

```typescript
// Example: Replace mock data with API
export const fetchTracks = async (): Promise<Track[]> => {
  const response = await fetch('/api/tracks');
  return response.json();
};
```

### Theming
Modify CSS custom properties in `src/index.css`:

```css
@theme {
  --color-gamma-primary: #8b5cf6;
  --color-gamma-accent: #06b6d4;
  /* ... */
}
```

### Adding New Features
1. Create new page in `src/pages/`
2. Add route in `src/App.tsx`
3. Use existing components from `src/components/ui/`
4. Follow the established design patterns

## 🗺️ Roadmap

### Phase 1 — Core (Current)
- ✅ Discover, Search, Library, Profile
- ✅ Player with queue management
- ✅ Favorites and recently played
- ✅ Local playlists
- ✅ Dark/light themes

### Phase 2 — Integration
- [ ] Music provider abstraction
- [ ] YouTube Data API integration
- [ ] Real-time search
- [ ] Artist/album metadata

### Phase 3 — Advanced Features
- [ ] Background playback
- [ ] Offline support (metadata only)
- [ ] Smart recommendations
- [ ] Social features
- [ ] Cross-device sync

### Phase 4 — Platform Expansion
- [ ] PWA support
- [ ] Mobile apps (React Native)
- [ ] Desktop apps (Electron)
- [ ] Smart watch companion

## 📄 License

This is a demonstration project. Music content is mock data for UI/UX purposes only.

## 🙏 Acknowledgments

- Design inspired by premium music applications
- Built with modern web technologies
- Focused on user experience and accessibility

## 📞 Support

For questions or issues, please refer to the documentation or create an issue in the repository.

---

**GAMMA — Your Personal Frequency**  
*Discover. Explore. Experience.*
