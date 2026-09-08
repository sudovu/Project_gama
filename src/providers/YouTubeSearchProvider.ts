import { Track, Artist, MusicProvider, PlaybackCapability } from '../types';

// Using Piped API - a public, open-source YouTube frontend
// No API key required. Compliant with YouTube ToS (uses official YouTube embed for playback)
const PIPED_INSTANCES = [
  'https://pipedapi.kavin.rocks',
  'https://pipedapi.adminforge.de',
  'https://api.piped.privacy.com.de',
];

let currentInstance = 0;

function getPipedUrl(): string {
  return PIPED_INSTANCES[currentInstance % PIPED_INSTANCES.length];
}

function rotateInstance(): void {
  currentInstance = (currentInstance + 1) % PIPED_INSTANCES.length;
}

interface PipedSearchResult {
  url: string;
  title: string;
  thumbnail: string;
  uploaderName: string;
  uploaderUrl: string;
  uploaderAvatar: string;
  duration: number;
  type: string;
  uploadedDate?: string;
  views?: number;
  isShort?: boolean;
}

interface PipedStreamInfo {
  title: string;
  description: string;
  uploadDate: string;
  uploader: string;
  uploaderUrl: string;
  uploaderAvatar: string;
  duration: number;
  views: number;
  likes: number;
  thumbnailUrl: string;
  category: string;
  hls?: string;
  dash?: string;
}

function extractVideoId(url: string): string {
  // Piped returns URLs like /watch?v=VIDEO_ID
  const match = url.match(/[?&]v=([^&]+)/);
  return match ? match[1] : url.replace('/watch?v=', '');
}

function mapPipedToTrack(item: PipedSearchResult): Track {
  const videoId = extractVideoId(item.url);
  return {
    id: `yt_${videoId}`,
    title: item.title || 'Unknown Title',
    artist: item.uploaderName || 'Unknown Artist',
    artistId: `yt_artist_${videoId}`,
    album: 'YouTube',
    albumId: 'yt_album',
    artwork: item.thumbnail || '',
    duration: item.duration || 0,
    genre: item.type || 'Music',
    mood: [],
    isFavorite: false,
    provider: 'youtube',
    videoId: videoId,
  };
}

export class YouTubeSearchProvider implements MusicProvider {
  private async fetchWithFallback(path: string): Promise<Response> {
    let lastError: Error | null = null;
    
    for (let attempt = 0; attempt < PIPED_INSTANCES.length; attempt++) {
      try {
        const baseUrl = getPipedUrl();
        const response = await fetch(`${baseUrl}${path}`, {
          signal: AbortSignal.timeout(8000),
        });
        
        if (response.ok) {
          return response;
        }
        
        lastError = new Error(`HTTP ${response.status}`);
        rotateInstance();
      } catch (error) {
        lastError = error as Error;
        rotateInstance();
      }
    }
    
    throw lastError || new Error('All instances unavailable');
  }

  async search(query: string, type?: 'track' | 'artist' | 'album' | 'playlist'): Promise<Track[]> {
    if (!query.trim()) return [];

    try {
      const searchType = type === 'artist' ? 'channel' : 'music_songs';
      const path = `/search?q=${encodeURIComponent(query)}&filter=${searchType}`;
      const response = await this.fetchWithFallback(path);
      const data = await response.json();

      const items: PipedSearchResult[] = data.items || [];
      return items
        .filter((item: PipedSearchResult) => item.type === 'stream' && item.duration > 0)
        .map(mapPipedToTrack);
    } catch (error) {
      console.error('YouTube search failed:', error);
      return [];
    }
  }

  async getTrack(id: string): Promise<Track | null> {
    const videoId = id.replace('yt_', '');
    try {
      const response = await this.fetchWithFallback(`/streams/${videoId}`);
      const data: PipedStreamInfo = await response.json();
      
      return {
        id: `yt_${videoId}`,
        title: data.title,
        artist: data.uploader,
        artistId: `yt_artist_${videoId}`,
        album: 'YouTube',
        albumId: 'yt_album',
        artwork: data.thumbnailUrl,
        duration: data.duration,
        genre: data.category || 'Music',
        mood: [],
        isFavorite: false,
        provider: 'youtube',
        videoId,
      };
    } catch {
      return null;
    }
  }

  async getArtist(id: string): Promise<Artist | null> {
    return null; // Not implemented for individual artists
  }

  async getPlaylist(id: string): Promise<any | null> {
    return null;
  }

  async getRecommendations(trackId: string): Promise<Track[]> {
    const videoId = trackId.replace('yt_', '');
    try {
      const response = await this.fetchWithFallback(`/streams/${videoId}`);
      const data = await response.json();
      const related = data.relatedStreams || [];
      return related
        .filter((item: PipedSearchResult) => item.type === 'stream' && item.duration > 0)
        .slice(0, 10)
        .map(mapPipedToTrack);
    } catch {
      return [];
    }
  }

  async getPlaybackCapability(_trackId: string): Promise<PlaybackCapability> {
    return {
      canPlay: true,
      canBackgroundPlay: false,
      canDownload: false,
      canCache: false,
      playbackMethod: 'iframe',
    };
  }

  getEmbedUrl(videoId: string): string {
    const cleanId = videoId.replace('yt_', '');
    return `https://www.youtube.com/embed/${cleanId}?autoplay=1&enablejsapi=1`;
  }

  // Get trending music
  async getTrending(): Promise<Track[]> {
    try {
      const response = await this.fetchWithFallback('/trending?region=US&type=music');
      const data = await response.json();
      return (data || [])
        .filter((item: PipedSearchResult) => item.duration > 0)
        .slice(0, 20)
        .map(mapPipedToTrack);
    } catch {
      return [];
    }
  }
}

export const youtubeProvider = new YouTubeSearchProvider();
