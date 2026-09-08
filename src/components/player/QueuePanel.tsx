import React from 'react';
import { X, Trash2, GripVertical } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { Track } from '../../types';
import { GammaArtwork } from '../ui/GammaArtwork';

interface QueuePanelProps {
  queue: Track[];
  currentIndex: number;
  onClose: () => void;
  onRemoveTrack: (index: number) => void;
  onClearQueue: () => void;
  onPlayTrack: (index: number) => void;
}

export const QueuePanel: React.FC<QueuePanelProps> = ({
  queue,
  currentIndex,
  onClose,
  onRemoveTrack,
  onClearQueue,
  onPlayTrack,
}) => {
  const currentTrack = queue[currentIndex];
  const upcomingTracks = queue.slice(currentIndex + 1);

  return (
    <motion.div
      initial={{ y: '100%' }}
      animate={{ y: 0 }}
      exit={{ y: '100%' }}
      transition={{ type: 'spring', damping: 30, stiffness: 300 }}
      className="fixed inset-0 z-[60] bg-gamma-bg flex flex-col"
    >
      {/* Header */}
      <div className="flex items-center justify-between px-6 py-4 border-b border-gamma-border">
        <h2 className="text-lg font-display font-bold text-gamma-text-primary">Queue</h2>
        <div className="flex items-center gap-2">
          {queue.length > 1 && (
            <button
              onClick={onClearQueue}
              className="p-2 rounded-full hover:bg-gamma-surface-hover transition-colors"
              aria-label="Clear queue"
            >
              <Trash2 size={18} className="text-gamma-text-secondary" />
            </button>
          )}
          <button
            onClick={onClose}
            className="p-2 rounded-full hover:bg-gamma-surface-hover transition-colors"
            aria-label="Close queue"
          >
            <X size={20} className="text-gamma-text-primary" />
          </button>
        </div>
      </div>

      {/* Queue Content */}
      <div className="flex-1 overflow-y-auto px-4 py-4">
        {/* Now Playing */}
        {currentTrack && (
          <div className="mb-6">
            <h3 className="text-xs font-medium text-gamma-text-muted uppercase tracking-wider mb-3 px-2">
              Now Playing
            </h3>
            <div className="flex items-center gap-3 p-3 rounded-xl bg-gamma-primary/10 border border-gamma-primary/20">
              <GammaArtwork seed={currentTrack.id} type="track" size="sm" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gamma-primary truncate">{currentTrack.title}</p>
                <p className="text-xs text-gamma-text-secondary truncate">{currentTrack.artist}</p>
              </div>
              <div className="flex items-end gap-0.5 h-4">
                <div className="w-0.5 bg-gamma-primary rounded-full animate-waveform" style={{ animationDelay: '0ms' }} />
                <div className="w-0.5 bg-gamma-primary rounded-full animate-waveform" style={{ animationDelay: '200ms' }} />
                <div className="w-0.5 bg-gamma-primary rounded-full animate-waveform" style={{ animationDelay: '400ms' }} />
              </div>
            </div>
          </div>
        )}

        {/* Up Next */}
        {upcomingTracks.length > 0 && (
          <div>
            <h3 className="text-xs font-medium text-gamma-text-muted uppercase tracking-wider mb-3 px-2">
              Up Next — {upcomingTracks.length} tracks
            </h3>
            <div className="space-y-1">
              <AnimatePresence>
                {upcomingTracks.map((track, index) => {
                  const actualIndex = currentIndex + 1 + index;
                  return (
                    <motion.div
                      key={track.id}
                      initial={{ opacity: 0, x: 20 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: -20 }}
                      transition={{ delay: index * 0.03 }}
                      className="flex items-center gap-3 p-2 rounded-xl hover:bg-gamma-surface-hover transition-colors group"
                    >
                      <GripVertical size={14} className="text-gamma-text-muted opacity-0 group-hover:opacity-100 transition-opacity cursor-grab" />
                      <GammaArtwork seed={track.id} type="track" size="sm" />
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gamma-text-primary truncate">{track.title}</p>
                        <p className="text-xs text-gamma-text-secondary truncate">{track.artist}</p>
                      </div>
                      <button
                        onClick={() => onRemoveTrack(actualIndex)}
                        className="p-1.5 rounded-full opacity-0 group-hover:opacity-100 hover:bg-gamma-surface-elevated transition-all"
                        aria-label={`Remove ${track.title} from queue`}
                      >
                        <X size={14} className="text-gamma-text-muted" />
                      </button>
                    </motion.div>
                  );
                })}
              </AnimatePresence>
            </div>
          </div>
        )}

        {/* Empty Queue */}
        {queue.length <= 1 && (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <p className="text-sm text-gamma-text-secondary">Queue is empty</p>
            <p className="text-xs text-gamma-text-muted mt-1">Add tracks to build your queue</p>
          </div>
        )}
      </div>
    </motion.div>
  );
};
