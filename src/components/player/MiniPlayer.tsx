import React from 'react';
import { Play, Pause, SkipForward } from 'lucide-react';
import { PlayerState } from '../../types';
import { GammaArtwork } from '../ui/GammaArtwork';
import { motion, AnimatePresence } from 'framer-motion';

interface MiniPlayerProps {
  state: PlayerState;
  onTogglePlay: () => void;
  onNext: () => void;
  onExpand: () => void;
}

export const MiniPlayer: React.FC<MiniPlayerProps> = ({ state, onTogglePlay, onNext, onExpand }) => {
  if (!state.currentTrack) return null;

  const progress = state.currentTrack.duration > 0 
    ? (state.progress / state.currentTrack.duration) * 100 
    : 0;

  return (
    <AnimatePresence>
      <motion.div
        initial={{ y: 100, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        exit={{ y: 100, opacity: 0 }}
        transition={{ type: 'spring', damping: 25, stiffness: 300 }}
        className="glass-panel rounded-2xl mx-3 mb-2 overflow-hidden"
      >
        {/* Progress bar */}
        <div className="h-0.5 bg-gamma-border/30">
          <motion.div 
            className="h-full gradient-primary"
            style={{ width: `${progress}%` }}
            transition={{ duration: 0.5 }}
          />
        </div>

        <div className="flex items-center gap-3 p-3">
          <div 
            className="cursor-pointer flex-shrink-0 w-11 h-11 rounded-xl overflow-hidden"
            onClick={onExpand}
          >
            {state.currentTrack.artwork && state.currentTrack.provider === 'youtube' ? (
              <img 
                src={state.currentTrack.artwork}
                alt={state.currentTrack.title}
                className="w-full h-full object-cover"
              />
            ) : (
              <GammaArtwork seed={state.currentTrack.id} type="track" size="sm" />
            )}
          </div>
          
          <div 
            className="flex-1 min-w-0 cursor-pointer"
            onClick={onExpand}
          >
            <p className="text-sm font-medium text-gamma-text-primary truncate">
              {state.currentTrack.title}
            </p>
            <p className="text-xs text-gamma-text-secondary truncate">
              {state.currentTrack.artist}
            </p>
          </div>

          <div className="flex items-center gap-1">
            <button
              onClick={onTogglePlay}
              className="p-2 rounded-full hover:bg-gamma-surface-hover transition-colors"
              aria-label={state.playbackState === 'playing' ? 'Pause' : 'Play'}
            >
              {state.playbackState === 'playing' ? (
                <Pause size={18} className="text-gamma-text-primary" />
              ) : (
                <Play size={18} className="text-gamma-text-primary fill-gamma-text-primary" />
              )}
            </button>
            <button
              onClick={onNext}
              className="p-2 rounded-full hover:bg-gamma-surface-hover transition-colors"
              aria-label="Next track"
            >
              <SkipForward size={18} className="text-gamma-text-secondary" />
            </button>
          </div>
        </div>
      </motion.div>
    </AnimatePresence>
  );
};
