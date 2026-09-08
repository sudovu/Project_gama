import { Track, Artist, Album, Playlist, MusicProvider, PlaybackCapability } from '../types';

/**
 * YouTube Provider Implementation
 * 
 * Uses official YouTube Data API v3 for search and metadata
 * Uses YouTube IFrame Player API for playback
 * 
 * IMPORTANT: This provider requires:
 * 1. YouTube Data API v3 key (from Google Cloud Console)
 * 2. Backend proxy to secure API keys
 * 3. OAuth 2.0 for authenticated features
 * 
 * Compliance:
 * - Uses only official YouTube APIs
 * - Does NOT extract stream URLs
 * - Does NOT bypass DRM or access controls
 * - Does NOT enable unauthorized downloading
 * - Does NOT circumvent advertising
 */

interface YouTubeSearchResponse {
  kind: string;
  items: YouTubeSearchItem[];
  pageInfo: {
    totalResults: number;
    resultsPerPage: number;
  };
}

interface YouTubeSearchItem {
  id: {
    kind: string;
    videoId?: string;
    channelId?: string;
    playlistId?: string;
  };
  snippet: {
    title: string;
    description: string;
    thumbnails: {
      default: { url: string };
      medium: { url: string };
      high: { url: string };
    };
    channelTitle: string;
    publishedAt: string;
  };
}

interface YouTubeVideoResponse {
  items: YouTubeVideo[];
}

interface YouTubeVideo {
  id: string;
  snippet: {
    title: string;
    description: string;
    channelTitle: string;
    channelId: string;
    thumbnails: {
      default: { url: string };
      medium: { url: string };
      high: { url: string };
    };
    tags?: string[];
    categoryId: string;
  };
  contentDetails: {
    duration: string;
  };
  statistics: {
    viewCount: string;
    likeCount: string;
  };
}

export class YouTubeProvider implements MusicProvider {
  private apiKey: string;
  private baseUrl = 'https://www.googleapis.com/youtube/v3';

  constructor(apiKey: string) {
    this.apiKey = apiKey;
  }

  /**
   * Search for content on YouTube
   * Uses YouTube Data API v3 search endpoint
   */
  async search(query: string, type: 'track' | 'artist' | 'album' | 'playlist' = 'track'): Promise<Track[]> {
    try {
      const searchType = this.getYouTubeSearchType(type);
      const response = await fetch(
        `${this.baseUrl}/search?part=snippet&q=${encodeURIComponent(query)}&type=${searchType}&maxResults=20&key=${this.apiKey}`
      );

      if (!response.ok) {
        throw new Error(`YouTube API error: ${response.status}`);
      }

      const data: YouTubeSearchResponse = await response.json();
      return data.items.map(item => this.mapSearchItemToTrack(item));
    } catch (error) {
      console.error('YouTube search error:', error);
      throw new Error('Failed to search YouTube');
    }
  }

  /**
   * Get track details by video ID
   */
  async getTrack(id: string): Promise<Track | null> {
    try {
      const response = await fetch(
        `${this.baseUrl}/videos?part=snippet,contentDetails,statistics&id=${id}&key=${this.apiKey}`
      );

      if (!response.ok) {
        throw new Error(`YouTube API error: ${response.status}`);
      }

      const data: YouTubeVideoResponse = await response.json();
      if (data.items.length === 0) return null;

      return this.mapVideoToTrack(data.items[0]);
    } catch (error) {
      console.error('YouTube getTrack error:', error);
      return null;
    }
  }

  /**
   * Get artist (channel) details
   */
  async getArtist(id: string): Promise<Artist | null> {
    try {
      const response = await fetch(
        `${this.baseUrl}/channels?part=snippet,statistics&id=${id}&key=${this.apiKey}`
      );

      if (!response.ok) {
        throw new Error(`YouTube API error: ${response.status}`);
      }

      const data = await response.json();
      if (data.items.length === 0) return null;

      const channel = data.items[0];
      return {
        id: channel.id,
        name: channel.snippet.title,
        artwork: channel.snippet.thumbnails.high?.url || channel.snippet.thumbnails.default.url,
        genre: 'YouTube Channel',
        followers: this.formatSubscriberCount(channel.statistics.subscriberCount),
        description: channel.snippet.description,
      };
    } catch (error) {
      console.error('YouTube getArtist error:', error);
      return null;
    }
  }

  /**
   * Get playlist details
   */
  async getPlaylist(id: string): Promise<Playlist | null> {
    try {
      const response = await fetch(
        `${this.baseUrl}/playlists?part=snippet,contentDetails&id=${id}&key=${this.apiKey}`
      );

      if (!response.ok) {
        throw new Error(`YouTube API error: ${response.status}`);
      }

      const data = await response.json();
      if (data.items.length === 0) return null;

      const playlist = data.items[0];
      return {
        id: playlist.id,
        title: playlist.snippet.title,
        description: playlist.snippet.description,
        artwork: playlist.snippet.thumbnails.high?.url || playlist.snippet.thumbnails.default.url,
        tracks: [], // Would need to fetch playlist items separately
        isUserCreated: false,
        source: 'youtube',
      };
    } catch (error) {
      console.error('YouTube getPlaylist error:', error);
      return null;
    }
  }

  /**
   * Get recommendations (related videos)
   */
  async getRecommendations(trackId: string): Promise<Track[]> {
    try {
      const response = await fetch(
        `${this.baseUrl}/search?part=snippet&relatedToVideoId=${trackId}&type=video&maxResults=10&key=${this.apiKey}`
      );

      if (!response.ok) {
        throw new Error(`YouTube API error: ${response.status}`);
      }

      const data: YouTubeSearchResponse = await response.json();
      return data.items.map(item => this.mapSearchItemToTrack(item));
    } catch (error) {
      console.error('YouTube recommendations error:', error);
      return [];
    }
  }

  /**
   * Get playback capability
   * YouTube IFrame Player API supports embedding but has restrictions
   */
  async getPlaybackCapability(trackId: string): Promise<PlaybackCapability> {
    // YouTube IFrame Player API allows embedding
    // But does NOT allow:
    // - Background playback (without Premium)
    // - Direct stream access
    // - Offline playback
    // - Ad-free playback (without Premium)
    
    return {
      canPlay: true,
      canBackgroundPlay: false, // Requires YouTube Premium
      canDownload: false, // Not permitted by ToS
      canCache: false, // Not permitted by ToS
      playbackMethod: 'iframe', // Must use official IFrame player
    };
  }

  /**
   * Get YouTube embed URL for IFrame player
   */
  getEmbedUrl(videoId: string): string {
    return `https://www.youtube.com/embed/${videoId}?autoplay=1&enablejsapi=1`;
  }

  // Helper methods

  private getYouTubeSearchType(type: string): string {
    switch (type) {
      case 'track':
        return 'video';
      case 'artist':
        return 'channel';
      case 'playlist':
        return 'playlist';
      default:
        return 'video';
    }
  }

  private mapSearchItemToTrack(item: YouTubeSearchItem): Track {
    return {
      id: item.id.videoId || '',
      title: item.snippet.title,
      artist: item.snippet.channelTitle,
      artistId: '', // Would need to fetch channel details
      album: '',
      albumId: '',
      artwork: item.snippet.thumbnails.high?.url || item.snippet.thumbnails.medium?.url || item.snippet.thumbnails.default.url,
      duration: 0, // Not available in search results
      genre: 'YouTube',
      mood: [],
      isFavorite: false,
    };
  }

  private mapVideoToTrack(video: YouTubeVideo): Track {
    return {
      id: video.id,
      title: video.snippet.title,
      artist: video.snippet.channelTitle,
      artistId: video.snippet.channelId,
      album: '',
      albumId: '',
      artwork: video.snippet.thumbnails.high?.url || video.snippet.thumbnails.medium?.url || video.snippet.thumbnails.default.url,
      duration: this.parseDuration(video.contentDetails.duration),
      genre: video.snippet.tags?.[0] || 'YouTube',
      mood: [],
      isFavorite: false,
    };
  }

  private parseDuration(isoDuration: string): number {
    // Parse ISO 8601 duration (PT4M13S) to seconds
    const match = isoDuration.match(/PT(\d+H)?(\d+M)?(\d+S)?/);
    if (!match) return 0;

    const hours = parseInt(match[1]?.replace('H', '') || '0');
    const minutes = parseInt(match[2]?.replace('M', '') || '0');
    const seconds = parseInt(match[3]?.replace('S', '') || '0');

    return hours * 3600 + minutes * 60 + seconds;
  }

  private formatSubscriberCount(count: string): string {
    const num = parseInt(count);
    if (num >= 1000000) {
      return `${(num / 1000000).toFixed(1)}M`;
    } else if (num >= 1000) {
      return `${(num / 1000).toFixed(1)}K`;
    }
    return count;
  }
}

/**
 * Mock YouTube Provider for development
 * Use this when API key is not available
 */
export class MockYouTubeProvider implements MusicProvider {
  async search(query: string): Promise<Track[]> {
    console.log(`[MockYouTube] Searching for: ${query}`);
    // Return mock data or empty array
    return [];
  }

  async getTrack(id: string): Promise<Track | null> {
    console.log(`[MockYouTube] Getting track: ${id}`);
    return null;
  }

  async getArtist(id: string): Promise<Artist | null> {
    console.log(`[MockYouTube] Getting artist: ${id}`);
    return null;
  }

  async getPlaylist(id: string): Promise<Playlist | null> {
    console.log(`[MockYouTube] Getting playlist: ${id}`);
    return null;
  }

  async getRecommendations(trackId: string): Promise<Track[]> {
    console.log(`[MockYouTube] Getting recommendations for: ${trackId}`);
    return [];
  }

  async getPlaybackCapability(trackId: string): Promise<PlaybackCapability> {
    return {
      canPlay: false,
      canBackgroundPlay: false,
      canDownload: false,
      canCache: false,
      playbackMethod: 'none',
    };
  }

  getEmbedUrl(videoId: string): string {
    return `https://www.youtube.com/embed/${videoId}`;
  }
}
