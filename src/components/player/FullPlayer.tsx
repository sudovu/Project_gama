import React, { useMemo } from 'react';
import { 
  Play, Pause, SkipBack, SkipForward, Shuffle, Repeat,
  Heart, ChevronDown, ListMusic
} from 'lucide-react';
import { PlayerState } from '../../types';
import { GammaArtwork } from '../ui/GammaArtwork';
import { motion, AnimatePresence } from 'framer-motion';

interface FullPlayerProps {
  state: PlayerState;
  isFavorite: boolean;
  onTogglePlay: () => void;
  onNext: () => void;
  onPrevious: () => void;
  onSeek: (progress: number) => void;
  onToggleShuffle: () => void;
  onCycleRepeat: () => void;
  onToggleFavorite: () => void;
  onCollapse: () => void;
  onShowQueue: () => void;
}

export const FullPlayer: React.FC<FullPlayerProps> = ({
  state,
  isFavorite,
  onTogglePlay,
  onNext,
  onPrevious,
  onSeek,
  onToggleShuffle,
  onCycleRepeat,
  onToggleFavorite,
  onCollapse,
  onShowQueue,
}) => {
  const track = state.currentTrack;
  if (!track) return null;

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const progress = track.duration > 0 ? (state.progress / track.duration) * 100 : 0;

  const gradientColors = useMemo(() => {
    const hash = track.id.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
    const colors = [
      ['#8b5cf6', '#1e1b4b'],
      ['#06b6d4', '#0c4a6e'],
      ['#d946ef', '#4a044e'],
      ['#10b981', '#064e3b'],
      ['#f59e0b', '#78350f'],
      ['#ef4444', '#7f1d1d'],
    ];
    return colors[hash % colors.length];
  }, [track.id]);

  return (
    <AnimatePresence>
      <motion.div
        initial={{ y: '100%' }}
        animate={{ y: 0 }}
        exit={{ y: '100%' }}
        transition={{ type: 'spring', damping: 30, stiffness: 300 }}
        className="fixed inset-0 z-50 flex flex-col"
        style={{
          background: `linear-gradient(180deg, ${gradientColors[1]} 0%, #050507 60%)`,
        }}
      >
        {/* Ambient background glow */}
        <div 
          className="absolute inset-0 opacity-20 pointer-events-none"
          style={{
            background: `radial-gradient(ellipse at 50% 30%, ${gradientColors[0]}40, transparent 70%)`,
          }}
        />

        {/* Header */}
        <div className="relative flex items-center justify-between px-6 pt-6 pb-2">
          <button
            onClick={onCollapse}
            className="p-2 rounded-full hover:bg-white/10 transition-colors"
            aria-label="Collapse player"
          >
            <ChevronDown size={24} className="text-gamma-text-primary" />
          </button>
          <div className="text-center">
            <p className="text-xs text-gamma-text-muted uppercase tracking-wider">Now Playing</p>
          </div>
          <button
            onClick={onShowQueue}
            className="p-2 rounded-full hover:bg-white/10 transition-colors"
            aria-label="Show queue"
          >
            <ListMusic size={20} className="text-gamma-text-primary" />
          </button>
        </div>

        {/* Artwork */}
        <div className="relative flex-1 flex items-center justify-center px-8">
          <motion.div
            key={track.id}
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.4 }}
            className="relative"
          >
            <div className="absolute inset-0 rounded-3xl blur-3xl opacity-40"
              style={{ background: gradientColors[0] }}
            />
            <GammaArtwork 
              seed={track.id} 
              type="track" 
              size="xl" 
              className="!w-64 !h-64 md:!w-72 md:!h-72 relative shadow-2xl"
            />
          </motion.div>
        </div>

        {/* Track Info & Controls */}
        <div className="relative px-8 pb-8 space-y-6">
          {/* Track Info */}
          <div className="flex items-center justify-between">
            <div className="min-w-0 flex-1">
              <motion.h2 
                key={track.title}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                className="text-xl font-display font-bold text-gamma-text-primary truncate"
              >
                {track.title}
              </motion.h2>
              <motion.p 
                key={track.artist}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.05 }}
                className="text-sm text-gamma-text-secondary mt-1"
              >
                {track.artist}
              </motion.p>
            </div>
            <button
              onClick={onToggleFavorite}
              className="p-2 rounded-full hover:bg-white/10 transition-colors ml-4"
              aria-label={isFavorite ? 'Remove from favorites' : 'Add to favorites'}
            >
              <Heart 
                size={22} 
                className={isFavorite ? 'text-gamma-primary fill-gamma-primary' : 'text-gamma-text-secondary'} 
              />
            </button>
          </div>

          {/* Progress Bar */}
          <div className="space-y-2">
            <div 
              className="relative h-1.5 bg-white/10 rounded-full cursor-pointer group"
              onClick={(e) => {
                const rect = e.currentTarget.getBoundingClientRect();
                const x = (e.clientX - rect.left) / rect.width;
                onSeek(Math.floor(x * track.duration));
              }}
            >
              <motion.div 
                className="absolute inset-y-0 left-0 rounded-full gradient-primary"
                style={{ width: `${progress}%` }}
              />
              <motion.div 
                className="absolute top-1/2 -translate-y-1/2 w-3.5 h-3.5 rounded-full bg-white shadow-lg opacity-0 group-hover:opacity-100 transition-opacity"
                style={{ left: `${progress}%`, marginLeft: '-7px' }}
              />
            </div>
            <div className="flex justify-between text-xs text-gamma-text-muted">
              <span>{formatTime(state.progress)}</span>
              <span>{formatTime(track.duration)}</span>
            </div>
          </div>

          {/* Controls */}
          <div className="flex items-center justify-between">
            <button
              onClick={onToggleShuffle}
              className="p-2 rounded-full hover:bg-white/10 transition-colors"
              aria-label="Toggle shuffle"
            >
              <Shuffle 
                size={18} 
                className={state.isShuffled ? 'text-gamma-primary' : 'text-gamma-text-secondary'} 
              />
            </button>

            <div className="flex items-center gap-4">
              <button
                onClick={onPrevious}
                className="p-2 rounded-full hover:bg-white/10 transition-colors"
                aria-label="Previous track"
              >
                <SkipBack size={24} className="text-gamma-text-primary fill-gamma-text-primary" />
              </button>

              <motion.button
                whileTap={{ scale: 0.9 }}
                onClick={onTogglePlay}
                className="p-4 rounded-full bg-white hover:bg-white/90 transition-colors shadow-lg"
                aria-label={state.playbackState === 'playing' ? 'Pause' : 'Play'}
              >
                {state.playbackState === 'playing' ? (
                  <Pause size={28} className="text-gamma-bg" />
                ) : (
                  <Play size={28} className="text-gamma-bg fill-gamma-bg" />
                )}
              </motion.button>

              <button
                onClick={onNext}
                className="p-2 rounded-full hover:bg-white/10 transition-colors"
                aria-label="Next track"
              >
                <SkipForward size={24} className="text-gamma-text-primary fill-gamma-text-primary" />
              </button>
            </div>

            <button
              onClick={onCycleRepeat}
              className="p-2 rounded-full hover:bg-white/10 transition-colors"
              aria-label="Toggle repeat"
            >
              <div className="relative">
                <Repeat size={18} className={state.repeatMode !== 'off' ? 'text-gamma-primary' : 'text-gamma-text-secondary'} />
                {state.repeatMode === 'one' && (
                  <span className="absolute -top-1 -right-1 text-[8px] font-bold text-gamma-primary">1</span>
                )}
              </div>
            </button>
          </div>
        </div>
      </motion.div>
    </AnimatePresence>
  );
};
