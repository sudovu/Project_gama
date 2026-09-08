import React, { useState, useCallback, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Compass, Search, Library, User } from 'lucide-react';
import { Track, Playlist } from './types';
import { usePlayer } from './hooks/usePlayer';
import { useLibrary } from './hooks/useLibrary';
import { useTheme } from './hooks/useTheme';
import { tracks, artists, albums, playlists } from './data/mockData';
import { SplashScreen } from './components/SplashScreen';
import { MiniPlayer } from './components/player/MiniPlayer';
import { FullPlayer } from './components/player/FullPlayer';
import { QueuePanel } from './components/player/QueuePanel';
import { YouTubePlayer } from './components/player/YouTubePlayer';
import { DiscoverPage } from './pages/DiscoverPage';
import { SearchPage } from './pages/SearchPage';
import { LibraryPage } from './pages/LibraryPage';
import { ProfilePage } from './pages/ProfilePage';
import { PlaylistDetail } from './pages/PlaylistDetail';
import { ArtistDetail } from './pages/ArtistDetail';
import { AlbumDetail } from './pages/AlbumDetail';

type Tab = 'discover' | 'search' | 'library' | 'profile';
type DetailView =
  | { type: 'playlist'; playlist: Playlist }
  | { type: 'artist'; artistId: string }
  | { type: 'album'; albumId: string }
  | null;

function App() {
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<Tab>('discover');
  const [showQueue, setShowQueue] = useState(false);
  const [detailView, setDetailView] = useState<DetailView>(null);
  const [showYouTubePlayer, setShowYouTubePlayer] = useState(false);

  const player = usePlayer();
  const library = useLibrary();
  const themeState = useTheme();

  const handlePlayTrack = useCallback((track: Track, queue?: Track[]) => {
    player.playTrack(track, queue);
    library.addToRecentlyPlayed(track);
    
    // Show YouTube player for YouTube tracks
    if (track.provider === 'youtube' && track.videoId) {
      setShowYouTubePlayer(true);
    }
  }, [player, library]);

  const handleToggleFavorite = useCallback((track: Track) => {
    library.toggleFavorite(track);
  }, [library]);

  const handleNavigateToPlaylist = useCallback((playlistId: string) => {
    const allPlaylists = [...playlists, ...library.userPlaylists];
    const playlist = allPlaylists.find(p => p.id === playlistId);
    if (playlist) setDetailView({ type: 'playlist', playlist });
  }, [library.userPlaylists]);

  const handleNavigateToArtist = useCallback((artistId: string) => {
    setDetailView({ type: 'artist', artistId });
  }, []);

  const handleNavigateToAlbum = useCallback((albumId: string) => {
    setDetailView({ type: 'album', albumId });
  }, []);

  const handleBackFromDetail = useCallback(() => {
    setDetailView(null);
  }, []);

  const handleSearch = useCallback((query: string) => {
    library.addSearchHistory(query);
  }, [library]);

  // Handle quick search events from Discover page
  useEffect(() => {
    const handleQuickSearch = (e: Event) => {
      const customEvent = e as CustomEvent<string>;
      setActiveTab('search');
      setDetailView(null);
      // Dispatch a custom event that SearchPage can listen to
      window.dispatchEvent(new CustomEvent('gamma-quick-search', { detail: customEvent.detail }));
    };
    window.addEventListener('gamma-search', handleQuickSearch);
    return () => window.removeEventListener('gamma-search', handleQuickSearch);
  }, []);

  const tabs: { id: Tab; label: string; icon: React.ElementType }[] = [
    { id: 'discover', label: 'Discover', icon: Compass },
    { id: 'search', label: 'Search', icon: Search },
    { id: 'library', label: 'Library', icon: Library },
    { id: 'profile', label: 'Profile', icon: User },
  ];

  if (isLoading) {
    return <SplashScreen onComplete={() => setIsLoading(false)} />;
  }

  const renderMainContent = () => {
    if (detailView) {
      switch (detailView.type) {
        case 'playlist':
          return (
            <PlaylistDetail
              playlist={detailView.playlist}
              onBack={handleBackFromDetail}
              onPlayTrack={handlePlayTrack}
              onPlayAll={() => {
                if (detailView.playlist.tracks.length > 0) {
                  handlePlayTrack(detailView.playlist.tracks[0], detailView.playlist.tracks);
                }
              }}
              onShuffle={() => {
                const shuffled = [...detailView.playlist.tracks].sort(() => Math.random() - 0.5);
                if (shuffled.length > 0) handlePlayTrack(shuffled[0], shuffled);
              }}
              isFavorite={library.isFavorite}
              onToggleFavorite={handleToggleFavorite}
              currentTrackId={player.state.currentTrack?.id ?? null}
              isPlaying={player.state.playbackState === 'playing'}
            />
          );
        case 'artist': {
          const artist = artists.find(a => a.id === detailView.artistId);
          if (!artist) return null;
          const artistTracks = tracks.filter(t => t.artistId === artist.id);
          return (
            <ArtistDetail
              artist={artist}
              allTracks={tracks}
              onBack={handleBackFromDetail}
              onPlayTrack={handlePlayTrack}
              onPlayAll={() => {
                if (artistTracks.length > 0) handlePlayTrack(artistTracks[0], artistTracks);
              }}
              onShuffle={() => {
                const shuffled = [...artistTracks].sort(() => Math.random() - 0.5);
                if (shuffled.length > 0) handlePlayTrack(shuffled[0], shuffled);
              }}
              isFavorite={library.isFavorite}
              onToggleFavorite={handleToggleFavorite}
              currentTrackId={player.state.currentTrack?.id ?? null}
              isPlaying={player.state.playbackState === 'playing'}
              onNavigateToAlbum={handleNavigateToAlbum}
            />
          );
        }
        case 'album': {
          const album = albums.find(a => a.id === detailView.albumId);
          if (!album) return null;
          return (
            <AlbumDetail
              album={album}
              onBack={handleBackFromDetail}
              onPlayTrack={handlePlayTrack}
              onPlayAll={() => {
                if (album.tracks.length > 0) handlePlayTrack(album.tracks[0], album.tracks);
              }}
              onShuffle={() => {
                const shuffled = [...album.tracks].sort(() => Math.random() - 0.5);
                if (shuffled.length > 0) handlePlayTrack(shuffled[0], shuffled);
              }}
              isFavorite={library.isFavorite}
              onToggleFavorite={handleToggleFavorite}
              currentTrackId={player.state.currentTrack?.id ?? null}
              isPlaying={player.state.playbackState === 'playing'}
              onNavigateToArtist={handleNavigateToArtist}
            />
          );
        }
        default:
          return null;
      }
    }

    switch (activeTab) {
      case 'discover':
        return (
          <DiscoverPage
            onPlayTrack={handlePlayTrack}
            onNavigateToArtist={handleNavigateToArtist}
            onNavigateToAlbum={handleNavigateToAlbum}
            onNavigateToPlaylist={handleNavigateToPlaylist}
          />
        );
      case 'search':
        return (
          <SearchPage
            onPlayTrack={handlePlayTrack}
            isFavorite={library.isFavorite}
            onToggleFavorite={handleToggleFavorite}
            searchHistory={library.searchHistory}
            onSearch={handleSearch}
            onClearHistory={library.clearSearchHistory}
            currentTrackId={player.state.currentTrack?.id ?? null}
            isPlaying={player.state.playbackState === 'playing'}
          />
        );
      case 'library':
        return (
          <LibraryPage
            favorites={library.favorites}
            recentlyPlayed={library.recentlyPlayed}
            userPlaylists={library.userPlaylists}
            onPlayTrack={handlePlayTrack}
            onToggleFavorite={handleToggleFavorite}
            isFavorite={library.isFavorite}
            onCreatePlaylist={library.createPlaylist}
            currentTrackId={player.state.currentTrack?.id ?? null}
            isPlaying={player.state.playbackState === 'playing'}
          />
        );
      case 'profile':
        return (
          <ProfilePage
            theme={themeState.theme}
            mode={themeState.mode}
            onToggleTheme={themeState.toggleMode}
            onSelectColor={themeState.setColorTheme}
            totalFavorites={library.favorites.length}
            totalPlaylists={playlists.length + library.userPlaylists.length}
            totalRecent={library.recentlyPlayed.length}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div className={`min-h-screen h-screen flex flex-col overflow-hidden ${themeState.mode === 'dark' ? 'bg-gamma-bg text-gamma-text-primary' : 'bg-gray-50 text-gray-900'}`}>
      {/* Ambient background effect */}
      {themeState.mode === 'dark' && (
        <div className="fixed inset-0 pointer-events-none overflow-hidden z-0">
          <div 
            className="absolute top-0 left-1/4 w-96 h-96 rounded-full blur-3xl animate-pulse-glow opacity-10"
            style={{ background: themeState.theme.primary }}
          />
          <div 
            className="absolute bottom-1/4 right-1/4 w-80 h-80 rounded-full blur-3xl animate-pulse-glow opacity-10"
            style={{ background: themeState.theme.accent, animationDelay: '1.5s' }}
          />
        </div>
      )}

      {/* Main content area */}
      <main className="flex-1 overflow-y-auto overflow-x-hidden relative z-10">
        <AnimatePresence mode="wait">
          <motion.div
            key={detailView ? `${detailView.type}-${detailView.type === 'playlist' ? detailView.playlist.id : detailView.type === 'artist' ? detailView.artistId : detailView.albumId}` : activeTab}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            transition={{ duration: 0.2 }}
          >
            {renderMainContent()}
          </motion.div>
        </AnimatePresence>
      </main>

      {/* YouTube Player */}
      {showYouTubePlayer && player.state.currentTrack?.videoId && (
        <div className="relative z-30 px-4 pb-2">
          <YouTubePlayer
            videoId={player.state.currentTrack.videoId}
            isPlaying={player.state.playbackState === 'playing'}
            onClose={() => setShowYouTubePlayer(false)}
          />
        </div>
      )}

      {/* Mini Player */}
      {player.state.currentTrack && !player.state.isExpanded && (
        <div className="relative z-20">
          <MiniPlayer
            state={player.state}
            onTogglePlay={player.togglePlay}
            onNext={player.handleNext}
            onExpand={player.toggleExpanded}
          />
        </div>
      )}

      {/* Full Player */}
      <AnimatePresence>
        {player.state.isExpanded && (
          <FullPlayer
            state={player.state}
            isFavorite={player.state.currentTrack ? library.isFavorite(player.state.currentTrack.id) : false}
            onTogglePlay={player.togglePlay}
            onNext={player.handleNext}
            onPrevious={player.handlePrevious}
            onSeek={player.seek}
            onToggleShuffle={player.toggleShuffle}
            onCycleRepeat={player.cycleRepeat}
            onToggleFavorite={() => player.state.currentTrack && handleToggleFavorite(player.state.currentTrack)}
            onCollapse={player.toggleExpanded}
            onShowQueue={() => setShowQueue(true)}
          />
        )}
      </AnimatePresence>

      {/* Queue Panel */}
      <AnimatePresence>
        {showQueue && (
          <QueuePanel
            queue={player.state.queue}
            currentIndex={player.state.queueIndex}
            onClose={() => setShowQueue(false)}
            onRemoveTrack={player.removeFromQueue}
            onClearQueue={player.clearQueue}
            onPlayTrack={(index) => {
              const track = player.state.queue[index];
              if (track) player.playQueue(player.state.queue, index);
            }}
          />
        )}
      </AnimatePresence>

      {/* Bottom Navigation */}
      {!player.state.isExpanded && !showQueue && (
        <nav className="relative z-20 glass-panel border-t border-gamma-border/50" role="navigation" aria-label="Main navigation">
          <div className="flex items-center justify-around py-2 px-2">
            {tabs.map((tab) => {
              const isActive = activeTab === tab.id && !detailView;
              return (
                <button
                  key={tab.id}
                  onClick={() => { setActiveTab(tab.id); setDetailView(null); }}
                  className={`relative flex flex-col items-center gap-1 px-5 py-2 rounded-xl transition-all duration-200 ${
                    isActive ? 'text-gamma-primary' : 'text-gamma-text-muted hover:text-gamma-text-secondary'
                  }`}
                  aria-label={tab.label}
                  aria-current={isActive ? 'page' : undefined}
                >
                  <tab.icon size={22} strokeWidth={isActive ? 2.5 : 1.5} />
                  <span className="text-[10px] font-medium">{tab.label}</span>
                  {isActive && (
                    <motion.div
                      layoutId="nav-indicator"
                      className="absolute -top-0.5 left-1/2 -translate-x-1/2 w-6 h-0.5 rounded-full gradient-primary"
                      transition={{ type: 'spring', stiffness: 400, damping: 30 }}
                    />
                  )}
                </button>
              );
            })}
          </div>
        </nav>
      )}
    </div>
  );
}

export default App;
