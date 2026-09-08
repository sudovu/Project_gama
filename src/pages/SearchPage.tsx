import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Search as SearchIcon, X, Clock, Play, Loader2, Wifi, WifiOff } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { Track } from '../types';
import { youtubeProvider } from '../providers/YouTubeSearchProvider';
import { tracks as mockTracks, artists, albums } from '../data/mockData';
import { GammaArtwork } from '../components/ui/GammaArtwork';
import { GammaSectionHeader } from '../components/ui/GammaSectionHeader';

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
  const [results, setResults] = useState<Track[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [error, setError] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Monitor online status
  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  // Focus input on mount
  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  // Listen for quick search events from Discover page
  useEffect(() => {
    const handleQuickSearch = (e: Event) => {
      const customEvent = e as CustomEvent<string>;
      const term = customEvent.detail;
      setQuery(term);
      performSearch(term);
    };
    window.addEventListener('gamma-quick-search', handleQuickSearch);
    return () => window.removeEventListener('gamma-quick-search', handleQuickSearch);
  }, []);

  // Debounced YouTube search
  const performSearch = useCallback(async (searchQuery: string) => {
    if (!searchQuery.trim()) {
      setResults([]);
      setHasSearched(false);
      return;
    }

    setIsSearching(true);
    setError(null);
    setHasSearched(true);

    try {
      // Search YouTube via Piped API
      const youtubeResults = await youtubeProvider.search(searchQuery, 'track');
      
      if (youtubeResults.length > 0) {
        setResults(youtubeResults);
      } else {
        // Fallback to mock data
        const q = searchQuery.toLowerCase();
        const mockResults = mockTracks.filter(
          t => t.title.toLowerCase().includes(q) || 
               t.artist.toLowerCase().includes(q) ||
               t.genre.toLowerCase().includes(q)
        );
        setResults(mockResults);
        if (mockResults.length === 0) {
          setError('No results found. Try a different search term.');
        }
      }
      
      onSearch(searchQuery);
    } catch {
      // Fallback to mock data on error
      const q = searchQuery.toLowerCase();
      const mockResults = mockTracks.filter(
        t => t.title.toLowerCase().includes(q) || 
             t.artist.toLowerCase().includes(q) ||
             t.genre.toLowerCase().includes(q)
      );
      setResults(mockResults);
      if (mockResults.length === 0) {
        setError('Search unavailable. Check your connection.');
      }
    } finally {
      setIsSearching(false);
    }
  }, [onSearch]);

  // Handle input change with debounce
  const handleInputChange = (value: string) => {
    setQuery(value);
    
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }
    
    if (value.trim().length >= 2) {
      debounceRef.current = setTimeout(() => {
        performSearch(value);
      }, 500);
    } else {
      setResults([]);
      setHasSearched(false);
      setError(null);
    }
  };

  const handleClear = () => {
    setQuery('');
    setResults([]);
    setHasSearched(false);
    setError(null);
    inputRef.current?.focus();
  };

  const formatDuration = (seconds: number) => {
    if (!seconds) return '';
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div className="pb-32 px-4 md:px-6 lg:px-8">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        className="pt-6 pb-4"
      >
        <h1 className="text-2xl md:text-3xl font-display font-bold text-gamma-text-primary">
          Search<span className="text-gamma-primary">.</span>
        </h1>
        <p className="text-gamma-text-secondary mt-1 text-sm">
          Search any song on YouTube
        </p>
      </motion.div>

      {/* Search Input */}
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.05 }}
        className="relative mb-6"
      >
        <div className="relative">
          <SearchIcon 
            size={18} 
            className="absolute left-4 top-1/2 -translate-y-1/2 text-gamma-text-muted" 
          />
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => handleInputChange(e.target.value)}
            placeholder="Search songs, artists, albums..."
            className="w-full pl-11 pr-20 py-3.5 bg-gamma-surface-elevated border border-gamma-border rounded-2xl text-gamma-text-primary placeholder:text-gamma-text-muted focus:outline-none focus:border-gamma-primary/50 focus:ring-1 focus:ring-gamma-primary/20 transition-all text-sm"
            aria-label="Search music"
          />
          <div className="absolute right-3 top-1/2 -translate-y-1/2 flex items-center gap-1">
            {query && (
              <button
                onClick={handleClear}
                className="p-1.5 rounded-full hover:bg-gamma-surface-hover transition-colors"
                aria-label="Clear search"
              >
                <X size={14} className="text-gamma-text-muted" />
              </button>
            )}
            <div className={`w-2 h-2 rounded-full ${isOnline ? 'bg-gamma-success' : 'bg-gamma-error'}`} 
              title={isOnline ? 'Online' : 'Offline'} 
            />
          </div>
        </div>
      </motion.div>

      {/* Search History */}
      <AnimatePresence mode="wait">
        {!hasSearched && searchHistory.length > 0 && (
          <motion.div
            key="history"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-xs font-medium text-gamma-text-muted uppercase tracking-wider">
                Recent Searches
              </h3>
              <button
                onClick={onClearHistory}
                className="text-xs text-gamma-primary hover:text-gamma-primary-light transition-colors"
              >
                Clear all
              </button>
            </div>
            <div className="flex flex-wrap gap-2">
              {searchHistory.slice(0, 10).map((item, index) => (
                <motion.button
                  key={item}
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ delay: index * 0.03 }}
                  onClick={() => {
                    setQuery(item);
                    performSearch(item);
                  }}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-gamma-surface-elevated border border-gamma-border text-sm text-gamma-text-secondary hover:bg-gamma-surface-hover hover:text-gamma-text-primary transition-colors"
                >
                  <Clock size={12} />
                  {item}
                </motion.button>
              ))}
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Browse when no search */}
      {!hasSearched && searchHistory.length === 0 && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.1 }}
        >
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <div className="w-20 h-20 rounded-3xl gradient-primary flex items-center justify-center mb-4 opacity-80">
              <SearchIcon size={32} className="text-white" />
            </div>
            <p className="text-gamma-text-primary font-medium text-lg">Find your next signal</p>
            <p className="text-sm text-gamma-text-secondary mt-2 max-w-xs">
              Search for any song, artist, or album. Results stream from YouTube.
            </p>
          </div>

          {/* Browse categories */}
          <GammaSectionHeader title="Browse" />
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {['Pop', 'Rock', 'Hip-Hop', 'Electronic', 'Jazz', 'Classical', 'R&B', 'Indie'].map((genre) => (
              <button
                key={genre}
                onClick={() => {
                  setQuery(genre);
                  performSearch(genre);
                }}
                className="p-4 rounded-2xl bg-gamma-surface-elevated border border-gamma-border hover:border-gamma-primary/30 transition-all text-left group"
              >
                <span className="text-sm font-medium text-gamma-text-primary group-hover:text-gamma-primary transition-colors">
                  {genre}
                </span>
              </button>
            ))}
          </div>
        </motion.div>
      )}

      {/* Loading State */}
      {isSearching && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="flex flex-col items-center justify-center py-12"
        >
          <Loader2 size={32} className="text-gamma-primary animate-spin" />
          <p className="text-sm text-gamma-text-secondary mt-4">Searching YouTube...</p>
        </motion.div>
      )}

      {/* Error State */}
      {error && !isSearching && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="flex flex-col items-center justify-center py-12 text-center"
        >
          <WifiOff size={32} className="text-gamma-text-muted mb-3" />
          <p className="text-gamma-text-primary font-medium">{error}</p>
          <button
            onClick={() => performSearch(query)}
            className="mt-4 px-4 py-2 rounded-lg bg-gamma-primary text-white text-sm font-medium hover:opacity-90 transition-opacity"
          >
            Retry
          </button>
        </motion.div>
      )}

      {/* Results */}
      {hasSearched && !isSearching && results.length > 0 && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
        >
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-xs font-medium text-gamma-text-muted uppercase tracking-wider">
              Results {results.length > 0 && `(${results.length})`}
            </h3>
            <div className="flex items-center gap-1.5 text-xs text-gamma-text-muted">
              <Wifi size={12} className="text-gamma-success" />
              <span>YouTube</span>
            </div>
          </div>
          <div className="space-y-1">
            {results.map((track, index) => (
              <motion.div
                key={track.id}
                initial={{ opacity: 0, x: -10 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: Math.min(index * 0.03, 0.3) }}
              >
                <div 
                  className={`group flex items-center gap-3 p-3 rounded-xl transition-all cursor-pointer ${
                    track.id === currentTrackId 
                      ? 'bg-gamma-primary/10 border border-gamma-primary/20' 
                      : 'hover:bg-gamma-surface-hover border border-transparent'
                  }`}
                  onClick={() => onPlayTrack(track, results)}
                  role="button"
                  tabIndex={0}
                  aria-label={`Play ${track.title} by ${track.artist}`}
                >
                  {/* Artwork */}
                  <div className="relative w-12 h-12 rounded-xl overflow-hidden flex-shrink-0">
                    {track.artwork ? (
                      <img 
                        src={track.artwork} 
                        alt={track.title}
                        className="w-full h-full object-cover"
                        loading="lazy"
                      />
                    ) : (
                      <GammaArtwork seed={track.id} type="track" size="sm" />
                    )}
                    
                    {/* Play overlay */}
                    <div className="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                      {track.id === currentTrackId && isPlaying ? (
                        <div className="flex items-end gap-0.5 h-4">
                          <div className="w-0.5 bg-white rounded-full animate-waveform" style={{ animationDelay: '0ms' }} />
                          <div className="w-0.5 bg-white rounded-full animate-waveform" style={{ animationDelay: '200ms' }} />
                          <div className="w-0.5 bg-white rounded-full animate-waveform" style={{ animationDelay: '400ms' }} />
                        </div>
                      ) : (
                        <Play size={16} className="text-white fill-white" />
                      )}
                    </div>
                  </div>

                  {/* Info */}
                  <div className="flex-1 min-w-0">
                    <p className={`text-sm font-medium truncate ${
                      track.id === currentTrackId ? 'text-gamma-primary' : 'text-gamma-text-primary'
                    }`}>
                      {track.title}
                    </p>
                    <p className="text-xs text-gamma-text-secondary truncate mt-0.5">
                      {track.artist}
                    </p>
                  </div>

                  {/* Duration */}
                  {track.duration > 0 && (
                    <span className="text-xs text-gamma-text-muted flex-shrink-0">
                      {formatDuration(track.duration)}
                    </span>
                  )}

                  {/* YouTube badge */}
                  <span className="hidden sm:flex items-center gap-1 px-2 py-0.5 rounded-full bg-red-500/10 text-red-400 text-[10px] font-medium flex-shrink-0">
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
                    </svg>
                    YouTube
                  </span>
                </div>
              </motion.div>
            ))}
          </div>
        </motion.div>
      )}

      {/* No results */}
      {hasSearched && !isSearching && results.length === 0 && !error && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="flex flex-col items-center justify-center py-12 text-center"
        >
          <SearchIcon size={32} className="text-gamma-text-muted mb-3" />
          <p className="text-gamma-text-primary font-medium">No results found</p>
          <p className="text-sm text-gamma-text-secondary mt-1">Try a different search term</p>
        </motion.div>
      )}
    </div>
  );
};
