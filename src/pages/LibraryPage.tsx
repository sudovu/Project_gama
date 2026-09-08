import React, { useState } from 'react';
import { Heart, Clock, ListMusic, Plus } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { Track, Playlist } from '../types';
import { GammaArtwork } from '../components/ui/GammaArtwork';
import { GammaTrackRow } from '../components/ui/GammaTrackRow';
import { GammaSectionHeader } from '../components/ui/GammaSectionHeader';
import { playlists } from '../data/mockData';

interface LibraryPageProps {
  favorites: Track[];
  recentlyPlayed: Track[];
  userPlaylists: Playlist[];
  onPlayTrack: (track: Track, queue?: Track[]) => void;
  onToggleFavorite: (track: Track) => void;
  isFavorite: (trackId: string) => boolean;
  onCreatePlaylist: (title: string, description: string) => void;
  currentTrackId: string | null;
  isPlaying: boolean;
}

type LibraryTab = 'favorites' | 'recent' | 'playlists';

export const LibraryPage: React.FC<LibraryPageProps> = ({
  favorites,
  recentlyPlayed,
  userPlaylists,
  onPlayTrack,
  onToggleFavorite,
  isFavorite,
  onCreatePlaylist,
  currentTrackId,
  isPlaying,
}) => {
  const [activeTab, setActiveTab] = useState<LibraryTab>('favorites');
  const [showCreatePlaylist, setShowCreatePlaylist] = useState(false);
  const [newPlaylistTitle, setNewPlaylistTitle] = useState('');
  const [newPlaylistDesc, setNewPlaylistDesc] = useState('');

  const handleCreatePlaylist = () => {
    if (newPlaylistTitle.trim()) {
      onCreatePlaylist(newPlaylistTitle, newPlaylistDesc);
      setNewPlaylistTitle('');
      setNewPlaylistDesc('');
      setShowCreatePlaylist(false);
    }
  };

  const tabs = [
    { id: 'favorites' as LibraryTab, label: 'Favorites', icon: Heart, count: favorites.length },
    { id: 'recent' as LibraryTab, label: 'Recent', icon: Clock, count: recentlyPlayed.length },
    { id: 'playlists' as LibraryTab, label: 'Playlists', icon: ListMusic, count: playlists.length + userPlaylists.length },
  ];

  return (
    <div className="pb-32 px-4 md:px-6 lg:px-8">
      {/* Header */}
      <div className="pt-6 pb-4">
        <h1 className="text-2xl md:text-3xl font-display font-bold text-gamma-text-primary">
          Your Library<span className="text-gamma-primary">.</span>
        </h1>
        <p className="text-gamma-text-secondary mt-1 text-sm">Your personal frequency space</p>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-6 overflow-x-auto pb-1">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium transition-all whitespace-nowrap ${
              activeTab === tab.id
                ? 'bg-gamma-primary text-white'
                : 'bg-gamma-surface-elevated text-gamma-text-secondary hover:bg-gamma-surface-hover'
            }`}
          >
            <tab.icon size={14} />
            {tab.label}
            <span className={`text-xs px-1.5 py-0.5 rounded-full ${
              activeTab === tab.id ? 'bg-white/20' : 'bg-gamma-border'
            }`}>
              {tab.count}
            </span>
          </button>
        ))}
      </div>

      {/* Favorites Tab */}
      <AnimatePresence mode="wait">
        {activeTab === 'favorites' && (
          <motion.div
            key="favorites"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
          >
            {favorites.length > 0 ? (
              <div className="space-y-1">
                {favorites.map((track) => (
                  <GammaTrackRow
                    key={track.id}
                    track={track}
                    isActive={track.id === currentTrackId}
                    isPlaying={track.id === currentTrackId && isPlaying}
                    isFavorite={true}
                    onPlay={() => onPlayTrack(track, favorites)}
                    onToggleFavorite={() => onToggleFavorite(track)}
                  />
                ))}
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center py-16 text-center">
                <div className="w-16 h-16 rounded-full bg-gamma-surface-elevated flex items-center justify-center mb-4">
                  <Heart size={24} className="text-gamma-text-muted" />
                </div>
                <p className="text-gamma-text-primary font-medium">No favorites yet</p>
                <p className="text-sm text-gamma-text-secondary mt-1">Heart tracks to save them here</p>
              </div>
            )}
          </motion.div>
        )}

        {/* Recent Tab */}
        {activeTab === 'recent' && (
          <motion.div
            key="recent"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
          >
            {recentlyPlayed.length > 0 ? (
              <div className="space-y-1">
                {recentlyPlayed.map((track) => (
                  <GammaTrackRow
                    key={track.id}
                    track={track}
                    isActive={track.id === currentTrackId}
                    isPlaying={track.id === currentTrackId && isPlaying}
                    isFavorite={isFavorite(track.id)}
                    onPlay={() => onPlayTrack(track, recentlyPlayed)}
                    onToggleFavorite={() => onToggleFavorite(track)}
                  />
                ))}
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center py-16 text-center">
                <div className="w-16 h-16 rounded-full bg-gamma-surface-elevated flex items-center justify-center mb-4">
                  <Clock size={24} className="text-gamma-text-muted" />
                </div>
                <p className="text-gamma-text-primary font-medium">No recent plays</p>
                <p className="text-sm text-gamma-text-secondary mt-1">Start listening to build your history</p>
              </div>
            )}
          </motion.div>
        )}

        {/* Playlists Tab */}
        {activeTab === 'playlists' && (
          <motion.div
            key="playlists"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
          >
            {/* Create Playlist Button */}
            <button
              onClick={() => setShowCreatePlaylist(!showCreatePlaylist)}
              className="flex items-center gap-3 w-full p-3 rounded-xl bg-gamma-surface hover:bg-gamma-surface-elevated border border-dashed border-gamma-border hover:border-gamma-primary/30 transition-all mb-4"
            >
              <div className="w-10 h-10 rounded-xl bg-gamma-primary/20 flex items-center justify-center">
                <Plus size={18} className="text-gamma-primary" />
              </div>
              <span className="text-sm font-medium text-gamma-text-primary">Create Playlist</span>
            </button>

            {/* Create Playlist Form */}
            <AnimatePresence>
              {showCreatePlaylist && (
                <motion.div
                  initial={{ height: 0, opacity: 0 }}
                  animate={{ height: 'auto', opacity: 1 }}
                  exit={{ height: 0, opacity: 0 }}
                  className="overflow-hidden mb-4"
                >
                  <div className="p-4 rounded-xl bg-gamma-surface-elevated border border-gamma-border space-y-3">
                    <input
                      type="text"
                      value={newPlaylistTitle}
                      onChange={(e) => setNewPlaylistTitle(e.target.value)}
                      placeholder="Playlist name"
                      className="w-full px-3 py-2 bg-gamma-surface border border-gamma-border rounded-lg text-sm text-gamma-text-primary placeholder:text-gamma-text-muted focus:outline-none focus:border-gamma-primary/50"
                    />
                    <input
                      type="text"
                      value={newPlaylistDesc}
                      onChange={(e) => setNewPlaylistDesc(e.target.value)}
                      placeholder="Description (optional)"
                      className="w-full px-3 py-2 bg-gamma-surface border border-gamma-border rounded-lg text-sm text-gamma-text-primary placeholder:text-gamma-text-muted focus:outline-none focus:border-gamma-primary/50"
                    />
                    <div className="flex gap-2">
                      <button
                        onClick={handleCreatePlaylist}
                        className="px-4 py-2 rounded-lg bg-gamma-primary text-white text-sm font-medium hover:bg-gamma-primary-light transition-colors"
                      >
                        Create
                      </button>
                      <button
                        onClick={() => setShowCreatePlaylist(false)}
                        className="px-4 py-2 rounded-lg bg-gamma-surface text-gamma-text-secondary text-sm hover:bg-gamma-surface-hover transition-colors"
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>

            {/* GAMMA Curated Playlists */}
            <GammaSectionHeader title="GAMMA Curated" />
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 mb-6">
              {playlists.map((playlist) => (
                <div key={playlist.id} className="text-left">
                  <GammaArtwork seed={playlist.id} type="playlist" size="lg" className="!w-full !h-28 mb-2" />
                  <p className="text-sm font-medium text-gamma-text-primary truncate">{playlist.title}</p>
                  <p className="text-xs text-gamma-text-secondary truncate">{playlist.tracks.length} tracks</p>
                </div>
              ))}
            </div>

            {/* User Playlists */}
            {userPlaylists.length > 0 && (
              <>
                <GammaSectionHeader title="Your Playlists" />
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                  {userPlaylists.map((playlist) => (
                    <div key={playlist.id} className="text-left">
                      <GammaArtwork seed={playlist.id} type="playlist" size="lg" className="!w-full !h-28 mb-2" />
                      <p className="text-sm font-medium text-gamma-text-primary truncate">{playlist.title}</p>
                      <p className="text-xs text-gamma-text-secondary truncate">{playlist.tracks.length} tracks</p>
                    </div>
                  ))}
                </div>
              </>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
