# YouTube Integration Guide for GAMMA

## Overview

GAMMA integrates with YouTube using only officially supported mechanisms:
- **YouTube Data API v3** for search and metadata
- **YouTube IFrame Player API** for playback
- **OAuth 2.0** for authenticated features

## Compliance Requirements

GAMMA strictly follows YouTube's Terms of Service:
- ✅ Uses official YouTube Data API v3
- ✅ Uses official YouTube IFrame Player API
- ✅ Implements proper OAuth 2.0 authentication
- ✅ Does NOT extract stream URLs
- ✅ Does NOT bypass DRM or access controls
- ✅ Does NOT circumvent advertising
- ✅ Does NOT enable unauthorized downloading

## Setup Instructions

### 1. Get YouTube Data API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable "YouTube Data API v3"
4. Create credentials (API Key)
5. Restrict the API key to your application
6. Copy the key to `.env`:
   ```
   YOUTUBE_API_KEY=your_api_key_here
   ```

### 2. Set Up OAuth 2.0 (Optional)

For authenticated features (user playlists, subscriptions):

1. In Google Cloud Console, go to "Credentials"
2. Create OAuth 2.0 Client ID
3. Configure authorized redirect URIs
4. Copy credentials to `.env`:
   ```
   YOUTUBE_CLIENT_ID=your_client_id
   YOUTUBE_CLIENT_SECRET=your_client_secret
   ```

### 3. API Usage

#### Search
```typescript
// Uses YouTube Data API v3
GET https://www.googleapis.com/youtube/v3/search
  ?part=snippet
  &q={query}
  &type=video
  &videoCategoryId=10 (Music)
  &key={API_KEY}
```

#### Video Details
```typescript
GET https://www.googleapis.com/youtube/v3/videos
  ?part=snippet,contentDetails,statistics
  &id={videoId}
  &key={API_KEY}
```

#### Playback
```typescript
// Uses YouTube IFrame Player API
// Embeds official YouTube player
<iframe
  src="https://www.youtube.com/embed/{videoId}"
  allow="autoplay; encrypted-media"
/>
```

## Limitations

Due to YouTube's Terms of Service:
- ❌ Cannot download videos/audio
- ❌ Cannot play in background (without Premium)
- ❌ Cannot bypass ads
- ❌ Cannot extract direct stream URLs
- ❌ Cannot cache media content

## Architecture

```
YouTubeProvider (implements MusicProvider)
  ↓
YouTube Data API v3 (search, metadata)
  ↓
YouTube IFrame Player API (playback)
  ↓
Domain Models (Track, Artist, Album)
  ↓
UI Components
```

## Rate Limits

YouTube Data API v3 has quotas:
- 10,000 units per day (free tier)
- Search: 100 units per request
- Video details: 1 unit per request

Implement caching and rate limiting to stay within quotas.

## Security

- Never expose API keys in client-side code
- Use a backend proxy for API calls
- Implement proper OAuth flow
- Store tokens securely
- Refresh tokens before expiration

## Next Steps

1. Obtain YouTube Data API v3 key
2. Set up backend proxy service
3. Implement YouTubeProvider
4. Test search functionality
5. Integrate IFrame player
6. Implement OAuth for authenticated features
