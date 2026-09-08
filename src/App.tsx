import React, { useState, useCallback } from 'react';
import { Home, Search, Library, User } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { NavigationTab, Track } from './types';
import { usePlayer } from './hooks/usePlayer';
import { useLibrary } from './hooks/useLibrary';
import { DiscoverPage } from './pages/DiscoverPage';
import { SearchPage } from './pages/SearchPage';
import { LibraryPage } from './pages/LibraryPage';
import { ProfilePage } from './pages/ProfilePage';
import { PlaylistDetail } from './pages/PlaylistDetail';
import { MiniPlayer } from './components/player/MiniPlayer';
import { FullPlayer } from './components/player/FullPlayer';
import { QueuePanel } from './components/player/QueuePanel';
import { SplashScreen } from './components/SplashScreen';
import { playlists as curatedPlaylists } from './data/mockData';

const navItems: { id: NavigationTab; label: string; icon: typeof Home }[] = [
  { id: 'discover', label: 'Discover', icon: Home },
  { id: 'search', label: 'Search', icon: Search },
  { id: 'library', label: 'Library', icon: Library },
  { id: 'profile', label: 'Profile', icon: User },
];

type DetailView = { type: 'playlist'; id: string } | null;

function App() {
  const [showSplash, setShowSplash] = useState(true);
  const [activeTab, setActiveTab] = useState<NavigationTab>('discover');
  const [theme, setTheme] = useState<'dark' | 'light'>('dark');
  const [showQueue, setShowQueue] = useState(false);
  const [detailView, setDetailView] = useState<DetailView>(null);
  const player = usePlayer();
  const library = useLibrary();

  const handlePlayTrack = useCallback((track: Track, queue?: Track[]) => {
    player.playTrack(track, queue);
    library.addToRecentlyPlayed(track);
  }, [player, library]);

  const handleToggleFavorite = useCallback((track: Track) => {
    library.toggleFavorite(track);
  }, [library]);

  const handleSearch = useCallback((query: string) => {
    library.addSearchHistory(query);
  }, [library]);

  const handleToggleTheme = useCallback(() => {
    setTheme(prev => prev === 'dark' ? 'light' : 'dark');
  }, []);

  const handleNavigateToPlaylist = useCallback((playlistId: string) => {
    setDetailView({ type: 'playlist', id: playlistId });
  }, []);

  const handleBackFromDetail = useCallback(() => {
    setDetailView(null);
  }, []);

  const currentPlaylist = detailView?.type === 'playlist' 
    ? [...curatedPlaylists, ...library.userPlaylists].find(p => p.id === detailView.id)
    : null;

  const renderContent = () => {
    if (detailView && currentPlaylist) {
      return (
        <motion.div
          key="detail"
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          exit={{ opacity: 0, x: -20 }}
          transition={{ duration: 0.2 }}
        >
          <PlaylistDetail
            playlist={currentPlaylist}
            onBack={handleBackFromDetail}
            onPlayTrack={handlePlayTrack}
            onPlayAll={() => {
              if (currentPlaylist.tracks.length > 0) {
                handlePlayTrack(currentPlaylist.tracks[0], currentPlaylist.tracks);
              }
            }}
            onShuffle={() => {
              if (currentPlaylist.tracks.length > 0) {
                const shuffled = [...currentPlaylist.tracks].sort(() => Math.random() - 0.5);
                handlePlayTrack(shuffled[0], shuffled);
              }
            }}
            isFavorite={library.isFavorite}
            onToggleFavorite={handleToggleFavorite}
            currentTrackId={player.state.currentTrack?.id || null}
            isPlaying={player.state.playbackState === 'playing'}
          />
        </motion.div>
      );
    }

    switch (activeTab) {
      case 'discover':
        return (
          <motion.div
            key="discover"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.2 }}
          >
            <DiscoverPage
              onPlayTrack={handlePlayTrack}
              onNavigateToArtist={() => {}}
              onNavigateToAlbum={() => {}}
              onNavigateToPlaylist={handleNavigateToPlaylist}
            />
          </motion.div>
        );
      case 'search':
        return (
          <motion.div
            key="search"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.2 }}
          >
            <SearchPage
              onPlayTrack={handlePlayTrack}
              isFavorite={library.isFavorite}
              onToggleFavorite={handleToggleFavorite}
              searchHistory={library.searchHistory}
              onSearch={handleSearch}
              onClearHistory={library.clearSearchHistory}
              currentTrackId={player.state.currentTrack?.id || null}
              isPlaying={player.state.playbackState === 'playing'}
            />
          </motion.div>
        );
      case 'library':
        return (
          <motion.div
            key="library"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.2 }}
          >
            <LibraryPage
              favorites={library.favorites}
              recentlyPlayed={library.recentlyPlayed}
              userPlaylists={library.userPlaylists}
              onPlayTrack={handlePlayTrack}
              onToggleFavorite={handleToggleFavorite}
              isFavorite={library.isFavorite}
              onCreatePlaylist={library.createPlaylist}
              currentTrackId={player.state.currentTrack?.id || null}
              isPlaying={player.state.playbackState === 'playing'}
            />
          </motion.div>
        );
      case 'profile':
        return (
          <motion.div
            key="profile"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.2 }}
          >
            <ProfilePage
              theme={theme}
              onToggleTheme={handleToggleTheme}
            />
          </motion.div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="h-screen w-screen flex flex-col bg-gamma-bg overflow-hidden relative">
      {/* Splash Screen */}
      <AnimatePresence>
        {showSplash && (
          <SplashScreen onComplete={() => setShowSplash(false)} />
        )}
      </AnimatePresence>

      {/* Ambient Background Effects */}
      <div className="fixed inset-0 pointer-events-none overflow-hidden">
        <div className="absolute top-0 left-1/4 w-96 h-96 bg-gamma-primary/5 rounded-full blur-[128px] animate-pulse-glow" />
        <div className="absolute bottom-1/4 right-1/4 w-80 h-80 bg-gamma-accent/5 rounded-full blur-[128px] animate-pulse-glow" style={{ animationDelay: '1.5s' }} />
      </div>

      {/* Main Content Area */}
      <div className="flex-1 overflow-y-auto overflow-x-hidden relative z-10">
        <AnimatePresence mode="wait">
          {renderContent()}
        </AnimatePresence>
      </div>

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
              if (track) {
                player.playQueue(player.state.queue, index);
              }
            }}
          />
        )}
      </AnimatePresence>

      {/* Bottom Navigation */}
      {!player.state.isExpanded && !showSplash && (
        <nav className="relative z-20 glass-panel border-t border-gamma-border/50" role="navigation" aria-label="Main navigation">
          <div className="flex items-center justify-around py-2 px-2">
            {navItems.map((item) => {
              const isActive = activeTab === item.id && !detailView;
              return (
                <button
                  key={item.id}
                  onClick={() => { setActiveTab(item.id); setDetailView(null); }}
                  className={`relative flex flex-col items-center gap-1 px-5 py-2 rounded-xl transition-all duration-200 ${
                    isActive ? 'text-gamma-primary' : 'text-gamma-text-muted hover:text-gamma-text-secondary'
                  }`}
                  aria-label={item.label}
                  aria-current={isActive ? 'page' : undefined}
                >
                  <item.icon size={22} strokeWidth={isActive ? 2.5 : 1.5} />
                  <span className="text-[10px] font-medium">{item.label}</span>
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
