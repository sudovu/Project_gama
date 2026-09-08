import React, { useState, useEffect, useRef } from 'react';
import { Search as SearchIcon, X, Clock } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { Track } from '../types';
import { GammaArtwork } from '../components/ui/GammaArtwork';
import { GammaTrackRow } from '../components/ui/GammaTrackRow';
import { GammaSectionHeader } from '../components/ui/GammaSectionHeader';
import { tracks, artists, albums, playlists } from '../data/mockData';

interface SearchPageProps {
  onPlayTrack: (track: Track, queue?: Track[]) => void;
  isFavorite: (trackId: string) => boolean;
  onToggleFavorite: (track: Track) => void;
  searchHistory: string[];
  onSearch: (query: string) => void;
  onClearHistory: () => void;
  currentTrackId: string | null;
  isPlaying: boolean;
}

export const SearchPage: React.FC<SearchPageProps> = ({
  onPlayTrack,
  isFavorite,
  onToggleFavorite,
  searchHistory,
  onSearch,
  onClearHistory,
  currentTrackId,
  isPlaying,
}) => {
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedQuery(query);
      if (query.trim()) {
        setIsSearching(true);
        setTimeout(() => setIsSearching(false), 300);
      }
    }, 300);
    return () => clearTimeout(timer);
  }, [query]);

  useEffect(() => {
    if (debouncedQuery.trim()) {
      onSearch(debouncedQuery);
    }
  }, [debouncedQuery, onSearch]);

  const handleClear = () => {
    setQuery('');
    setDebouncedQuery('');
    inputRef.current?.focus();
  };

  const filteredTracks = debouncedQuery.trim() 
    ? tracks.filter(t => 
        t.title.toLowerCase().includes(debouncedQuery.toLowerCase()) ||
        t.artist.toLowerCase().includes(debouncedQuery.toLowerCase()) ||
        t.genre.toLowerCase().includes(debouncedQuery.toLowerCase())
      )
    : [];

  const filteredArtists = debouncedQuery.trim()
    ? artists.filter(a => 
        a.name.toLowerCase().includes(debouncedQuery.toLowerCase()) ||
        a.genre.toLowerCase().includes(debouncedQuery.toLowerCase())
      )
    : [];

  const filteredAlbums = debouncedQuery.trim()
    ? albums.filter(a => 
        a.title.toLowerCase().includes(debouncedQuery.toLowerCase()) ||
        a.artist.toLowerCase().includes(debouncedQuery.toLowerCase())
      )
    : [];

  const showResults = debouncedQuery.trim().length > 0;
  const showEmpty = showResults && filteredTracks.length === 0 && filteredArtists.length === 0 && filteredAlbums.length === 0;

  return (
    <div className="pb-32 px-4 md:px-6 lg:px-8">
      {/* Search Header */}
      <div className="pt-6 pb-4">
        <h1 className="text-2xl md:text-3xl font-display font-bold text-gamma-text-primary mb-4">
          Search<span className="text-gamma-primary">.</span>
        </h1>
        
        {/* Search Input */}
        <div className="relative">
          <SearchIcon size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-gamma-text-muted" />
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search tracks, artists, albums..."
            className="w-full pl-11 pr-10 py-3.5 bg-gamma-surface-elevated border border-gamma-border rounded-2xl text-gamma-text-primary placeholder:text-gamma-text-muted focus:outline-none focus:border-gamma-primary/50 focus:ring-1 focus:ring-gamma-primary/20 transition-all text-sm"
            aria-label="Search music"
          />
          {query && (
            <button
              onClick={handleClear}
              className="absolute right-4 top-1/2 -translate-y-1/2 p-1 rounded-full hover:bg-gamma-surface-hover transition-colors"
              aria-label="Clear search"
            >
              <X size={16} className="text-gamma-text-secondary" />
            </button>
          )}
        </div>
      </div>

      {/* Search History (when no query) */}
      {!showResults && searchHistory.length > 0 && (
        <div className="mb-6">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-sm font-medium text-gamma-text-secondary flex items-center gap-2">
              <Clock size={14} /> Recent Searches
            </h3>
            <button 
              onClick={onClearHistory}
              className="text-xs text-gamma-text-muted hover:text-gamma-primary transition-colors"
            >
              Clear all
            </button>
          </div>
          <div className="flex flex-wrap gap-2">
            {searchHistory.map((item, index) => (
              <button
                key={index}
                onClick={() => setQuery(item)}
                className="px-3 py-1.5 rounded-full bg-gamma-surface-elevated border border-gamma-border text-sm text-gamma-text-secondary hover:border-gamma-primary/30 hover:text-gamma-primary transition-all"
              >
                {item}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Browse Categories (when no query) */}
      {!showResults && (
        <div>
          <GammaSectionHeader title="Browse All" />
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
            {['Electronic', 'Ambient', 'Synthwave', 'Techno', 'Deep House', 'Post-Rock', 'Indie Electronic', 'Dark Ambient', 'Chill'].map((genre, i) => {
              const gradients = [
                'from-violet-600 to-indigo-800',
                'from-cyan-500 to-blue-800',
                'from-fuchsia-500 to-purple-800',
                'from-emerald-500 to-teal-800',
                'from-amber-500 to-orange-800',
                'from-rose-500 to-pink-800',
                'from-sky-500 to-indigo-800',
                'from-purple-600 to-violet-900',
                'from-teal-500 to-cyan-800',
              ];
              return (
                <button
                  key={genre}
                  onClick={() => setQuery(genre)}
                  className={`relative overflow-hidden rounded-2xl p-4 bg-gradient-to-br ${gradients[i % gradients.length]} text-left hover:scale-[1.02] transition-transform`}
                >
                  <span className="text-sm font-medium text-white">{genre}</span>
                </button>
              );
            })}
          </div>
        </div>
      )}

      {/* Loading State */}
      {isSearching && (
        <div className="flex items-center justify-center py-12">
          <div className="flex gap-1">
            <div className="w-2 h-2 rounded-full bg-gamma-primary animate-pulse" />
            <div className="w-2 h-2 rounded-full bg-gamma-primary animate-pulse" style={{ animationDelay: '150ms' }} />
            <div className="w-2 h-2 rounded-full bg-gamma-primary animate-pulse" style={{ animationDelay: '300ms' }} />
          </div>
        </div>
      )}

      {/* Results */}
      <AnimatePresence mode="wait">
        {showResults && !isSearching && (
          <motion.div
            key="results"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
          >
            {/* Tracks */}
            {filteredTracks.length > 0 && (
              <div className="mb-6">
                <GammaSectionHeader title="Tracks" />
                <div className="space-y-1">
                  {filteredTracks.map((track) => (
                    <GammaTrackRow
                      key={track.id}
                      track={track}
                      isActive={track.id === currentTrackId}
                      isPlaying={track.id === currentTrackId && isPlaying}
                      isFavorite={isFavorite(track.id)}
                      onPlay={() => onPlayTrack(track, filteredTracks)}
                      onToggleFavorite={() => onToggleFavorite(track)}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Artists */}
            {filteredArtists.length > 0 && (
              <div className="mb-6">
                <GammaSectionHeader title="Artists" />
                <div className="flex gap-4 overflow-x-auto pb-2">
                  {filteredArtists.map((artist) => (
                    <div key={artist.id} className="flex-shrink-0 w-28 text-center">
                      <GammaArtwork seed={artist.id} type="artist" size="lg" className="!w-20 !h-20 mx-auto mb-2 rounded-full" rounded />
                      <p className="text-sm font-medium text-gamma-text-primary truncate">{artist.name}</p>
                      <p className="text-xs text-gamma-text-muted">{artist.genre}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Albums */}
            {filteredAlbums.length > 0 && (
              <div className="mb-6">
                <GammaSectionHeader title="Albums" />
                <div className="flex gap-4 overflow-x-auto pb-2">
                  {filteredAlbums.map((album) => (
                    <div key={album.id} className="flex-shrink-0 w-36">
                      <GammaArtwork seed={album.id} type="album" size="lg" className="!w-32 !h-32 mb-2" />
                      <p className="text-sm font-medium text-gamma-text-primary truncate">{album.title}</p>
                      <p className="text-xs text-gamma-text-secondary truncate">{album.artist}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Empty State */}
            {showEmpty && (
              <div className="flex flex-col items-center justify-center py-16 text-center">
                <div className="w-16 h-16 rounded-full bg-gamma-surface-elevated flex items-center justify-center mb-4">
                  <SearchIcon size={24} className="text-gamma-text-muted" />
                </div>
                <p className="text-gamma-text-primary font-medium">No results found</p>
                <p className="text-sm text-gamma-text-secondary mt-1">Try a different search term</p>
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
