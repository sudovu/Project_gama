import React from 'react';
import { Play, Shuffle, ArrowLeft, Clock, MoreHorizontal } from 'lucide-react';
import { motion } from 'framer-motion';
import { Track, Playlist } from '../types';
import { GammaArtwork } from '../components/ui/GammaArtwork';
import { GammaTrackRow } from '../components/ui/GammaTrackRow';

interface PlaylistDetailProps {
  playlist: Playlist;
  onBack: () => void;
  onPlayTrack: (track: Track, queue?: Track[]) => void;
  onPlayAll: () => void;
  onShuffle: () => void;
  isFavorite: (trackId: string) => boolean;
  onToggleFavorite: (track: Track) => void;
  currentTrackId: string | null;
  isPlaying: boolean;
}

export const PlaylistDetail: React.FC<PlaylistDetailProps> = ({
  playlist,
  onBack,
  onPlayTrack,
  onPlayAll,
  onShuffle,
  isFavorite,
  onToggleFavorite,
  currentTrackId,
  isPlaying,
}) => {
  const totalDuration = playlist.tracks.reduce((sum, t) => sum + t.duration, 0);
  const hours = Math.floor(totalDuration / 3600);
  const minutes = Math.floor((totalDuration % 3600) / 60);
  const durationText = hours > 0 ? `${hours}h ${minutes}m` : `${minutes} min`;

  return (
    <div className="pb-32">
      {/* Header with artwork */}
      <div className="relative">
        {/* Background gradient */}
        <div className="absolute inset-0 h-80 bg-gradient-to-b from-gamma-primary/20 to-transparent" />
        
        {/* Nav bar */}
        <div className="relative z-10 flex items-center justify-between px-4 pt-6 pb-4">
          <button
            onClick={onBack}
            className="p-2 rounded-full bg-gamma-bg/50 hover:bg-gamma-bg/80 transition-colors"
            aria-label="Go back"
          >
            <ArrowLeft size={20} className="text-gamma-text-primary" />
          </button>
          <button
            className="p-2 rounded-full bg-gamma-bg/50 hover:bg-gamma-bg/80 transition-colors"
            aria-label="More options"
          >
            <MoreHorizontal size={20} className="text-gamma-text-primary" />
          </button>
        </div>

        {/* Artwork and info */}
        <div className="relative z-10 flex flex-col items-center pt-4 pb-8 px-6">
          <motion.div
            initial={{ scale: 0.8, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.4 }}
          >
            <GammaArtwork 
              seed={playlist.id} 
              type="playlist" 
              size="xl" 
              className="!w-48 !h-48 md:!w-56 md:!h-56 shadow-2xl shadow-gamma-primary/20"
            />
          </motion.div>
          
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.15 }}
            className="text-center mt-6"
          >
            <h1 className="text-2xl md:text-3xl font-display font-bold text-gamma-text-primary">
              {playlist.title}
            </h1>
            <p className="text-sm text-gamma-text-secondary mt-2">{playlist.description}</p>
            <p className="text-xs text-gamma-text-muted mt-2">
              {playlist.tracks.length} tracks • {durationText}
            </p>
          </motion.div>

          {/* Action buttons */}
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.25 }}
            className="flex items-center gap-4 mt-6"
          >
            <button
              onClick={onPlayAll}
              className="flex items-center gap-2 px-6 py-3 rounded-full gradient-primary text-white font-medium text-sm hover:opacity-90 transition-opacity shadow-lg shadow-gamma-primary/30"
            >
              <Play size={18} className="fill-white" />
              Play All
            </button>
            <button
              onClick={onShuffle}
              className="flex items-center gap-2 px-5 py-3 rounded-full bg-gamma-surface-elevated border border-gamma-border text-gamma-text-primary font-medium text-sm hover:bg-gamma-surface-hover transition-colors"
            >
              <Shuffle size={16} />
              Shuffle
            </button>
          </motion.div>
        </div>
      </div>

      {/* Track List */}
      <div className="px-4 md:px-6">
        <div className="flex items-center justify-between mb-3 px-3">
          <span className="text-xs font-medium text-gamma-text-muted uppercase tracking-wider">Tracks</span>
          <span className="text-xs text-gamma-text-muted flex items-center gap-1">
            <Clock size={12} /> {durationText}
          </span>
        </div>
        <div className="space-y-1">
          {playlist.tracks.map((track, index) => (
            <GammaTrackRow
              key={track.id}
              track={track}
              index={index}
              showIndex
              isActive={track.id === currentTrackId}
              isPlaying={track.id === currentTrackId && isPlaying}
              isFavorite={isFavorite(track.id)}
              onPlay={() => onPlayTrack(track, playlist.tracks)}
              onToggleFavorite={() => onToggleFavorite(track)}
            />
          ))}
        </div>
      </div>
    </div>
  );
};
