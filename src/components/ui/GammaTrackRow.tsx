import React from 'react';
import { Play, Heart, MoreHorizontal } from 'lucide-react';
import { Track } from '../../types';
import { GammaArtwork } from './GammaArtwork';

interface GammaTrackRowProps {
  track: Track;
  index?: number;
  isActive?: boolean;
  isPlaying?: boolean;
  isFavorite?: boolean;
  onPlay?: () => void;
  onToggleFavorite?: () => void;
  onMore?: () => void;
  showIndex?: boolean;
}

export const GammaTrackRow: React.FC<GammaTrackRowProps> = ({
  track,
  index,
  isActive = false,
  isPlaying = false,
  isFavorite = false,
  onPlay,
  onToggleFavorite,
  onMore,
  showIndex = false,
}) => {
  const formatDuration = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div 
      className={`group flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 cursor-pointer ${
        isActive 
          ? 'bg-gamma-primary/10 border border-gamma-primary/20' 
          : 'hover:bg-gamma-surface-hover border border-transparent'
      }`}
      onClick={onPlay}
      role="button"
      tabIndex={0}
      aria-label={`Play ${track.title} by ${track.artist}`}
    >
      {showIndex && (
        <div className="w-6 text-center">
          {isActive && isPlaying ? (
            <div className="flex items-end justify-center gap-0.5 h-4">
              <div className="w-0.5 bg-gamma-primary rounded-full animate-waveform" style={{ animationDelay: '0ms' }} />
              <div className="w-0.5 bg-gamma-primary rounded-full animate-waveform" style={{ animationDelay: '200ms' }} />
              <div className="w-0.5 bg-gamma-primary rounded-full animate-waveform" style={{ animationDelay: '400ms' }} />
            </div>
          ) : (
            <span className={`text-sm ${isActive ? 'text-gamma-primary' : 'text-gamma-text-muted'}`}>
              {(index ?? 0) + 1}
            </span>
          )}
        </div>
      )}
      
      <GammaArtwork seed={track.id} type="track" size="sm" />
      
      <div className="flex-1 min-w-0">
        <p className={`text-sm font-medium truncate ${isActive ? 'text-gamma-primary' : 'text-gamma-text-primary'}`}>
          {track.title}
        </p>
        <p className="text-xs text-gamma-text-secondary truncate">
          {track.artist}
        </p>
      </div>

      <span className="text-xs text-gamma-text-muted hidden sm:block">
        {formatDuration(track.duration)}
      </span>

      <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
        <button
          onClick={(e) => { e.stopPropagation(); onToggleFavorite?.(); }}
          className="p-1.5 rounded-full hover:bg-gamma-surface-elevated transition-colors"
          aria-label={isFavorite ? 'Remove from favorites' : 'Add to favorites'}
        >
          <Heart 
            size={14} 
            className={isFavorite ? 'text-gamma-primary fill-gamma-primary' : 'text-gamma-text-secondary'} 
          />
        </button>
        <button
          onClick={(e) => { e.stopPropagation(); onMore?.(); }}
          className="p-1.5 rounded-full hover:bg-gamma-surface-elevated transition-colors"
          aria-label="More options"
        >
          <MoreHorizontal size={14} className="text-gamma-text-secondary" />
        </button>
      </div>

      {isActive && (
        <div className="absolute right-3">
          <Play size={14} className="text-gamma-primary fill-gamma-primary" />
        </div>
      )}
    </div>
  );
};
