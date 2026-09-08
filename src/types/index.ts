export interface Track {
  id: string;
  title: string;
  artist: string;
  artistId: string;
  album: string;
  albumId: string;
  artwork: string;
  duration: number; // seconds
  genre: string;
  mood: string[];
  isFavorite: boolean;
}

export interface Artist {
  id: string;
  name: string;
  artwork: string;
  genre: string;
  followers: string;
  description: string;
}

export interface Album {
  id: string;
  title: string;
  artist: string;
  artistId: string;
  artwork: string;
  year: number;
  tracks: Track[];
  genre: string;
}

export interface Playlist {
  id: string;
  title: string;
  description: string;
  artwork: string;
  tracks: Track[];
  isUserCreated: boolean;
  source: 'gamma' | 'user';
}

export interface SearchResult {
  tracks: Track[];
  artists: Artist[];
  albums: Album[];
  playlists: Playlist[];
}

export type PlaybackState = 'idle' | 'loading' | 'playing' | 'paused';
export type RepeatMode = 'off' | 'all' | 'one';

export interface PlayerState {
  currentTrack: Track | null;
  queue: Track[];
  queueIndex: number;
  playbackState: PlaybackState;
  progress: number;
  volume: number;
  isShuffled: boolean;
  repeatMode: RepeatMode;
  isExpanded: boolean;
}

export type NavigationTab = 'discover' | 'search' | 'library' | 'profile';

export interface MoodCategory {
  id: string;
  name: string;
  gradient: string;
  icon: string;
}
