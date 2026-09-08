import { useState, useCallback } from 'react';
import { Track, Playlist } from '../types';
import { tracks as initialTracks, playlists as initialPlaylists } from '../data/mockData';

export function useLibrary() {
  const [favorites, setFavorites] = useState<Track[]>(
    initialTracks.filter(t => t.isFavorite)
  );
  const [recentlyPlayed, setRecentlyPlayed] = useState<Track[]>([]);
  const [userPlaylists, setUserPlaylists] = useState<Playlist[]>([]);
  const [searchHistory, setSearchHistory] = useState<string[]>([]);

  const toggleFavorite = useCallback((track: Track) => {
    setFavorites(prev => {
      const exists = prev.find(t => t.id === track.id);
      if (exists) {
        return prev.filter(t => t.id !== track.id);
      }
      return [...prev, track];
    });
  }, []);

  const isFavorite = useCallback((trackId: string) => {
    return favorites.some(t => t.id === trackId);
  }, [favorites]);

  const addToRecentlyPlayed = useCallback((track: Track) => {
    setRecentlyPlayed(prev => {
      const filtered = prev.filter(t => t.id !== track.id);
      return [track, ...filtered].slice(0, 50);
    });
  }, []);

  const createPlaylist = useCallback((title: string, description: string) => {
    const newPlaylist: Playlist = {
      id: `up_${Date.now()}`,
      title,
      description,
      artwork: '',
      tracks: [],
      isUserCreated: true,
      source: 'user',
    };
    setUserPlaylists(prev => [...prev, newPlaylist]);
    return newPlaylist.id;
  }, []);

  const deletePlaylist = useCallback((playlistId: string) => {
    setUserPlaylists(prev => prev.filter(p => p.id !== playlistId));
  }, []);

  const addToPlaylist = useCallback((playlistId: string, track: Track) => {
    setUserPlaylists(prev =>
      prev.map(p => {
        if (p.id === playlistId) {
          if (p.tracks.some(t => t.id === track.id)) return p;
          return { ...p, tracks: [...p.tracks, track] };
        }
        return p;
      })
    );
  }, []);

  const removeFromPlaylist = useCallback((playlistId: string, trackId: string) => {
    setUserPlaylists(prev =>
      prev.map(p => {
        if (p.id === playlistId) {
          return { ...p, tracks: p.tracks.filter(t => t.id !== trackId) };
        }
        return p;
      })
    );
  }, []);

  const addSearchHistory = useCallback((query: string) => {
    if (!query.trim()) return;
    setSearchHistory(prev => {
      const filtered = prev.filter(q => q !== query);
      return [query, ...filtered].slice(0, 20);
    });
  }, []);

  const clearSearchHistory = useCallback(() => {
    setSearchHistory([]);
  }, []);

  return {
    favorites,
    recentlyPlayed,
    userPlaylists,
    searchHistory,
    toggleFavorite,
    isFavorite,
    addToRecentlyPlayed,
    createPlaylist,
    deletePlaylist,
    addToPlaylist,
    removeFromPlaylist,
    addSearchHistory,
    clearSearchHistory,
  };
}
