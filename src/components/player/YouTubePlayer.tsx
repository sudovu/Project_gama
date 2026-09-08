import React, { useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X } from 'lucide-react';

interface YouTubePlayerProps {
  videoId: string | null;
  isPlaying: boolean;
  onClose: () => void;
}

export const YouTubePlayer: React.FC<YouTubePlayerProps> = ({ videoId, isPlaying, onClose }) => {
  const iframeRef = useRef<HTMLIFrameElement>(null);

  // Send play/pause commands to YouTube IFrame API
  useEffect(() => {
    if (!iframeRef.current?.contentWindow) return;
    
    const command = isPlaying ? 'playVideo' : 'pauseVideo';
    iframeRef.current.contentWindow.postMessage(
      JSON.stringify({
        event: 'command',
        func: command,
        args: [],
      }),
      '*'
    );
  }, [isPlaying]);

  if (!videoId) return null;

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0, height: 0 }}
        animate={{ opacity: 1, height: 'auto' }}
        exit={{ opacity: 0, height: 0 }}
        className="overflow-hidden"
      >
        <div className="relative w-full aspect-video bg-black rounded-xl overflow-hidden">
          <iframe
            ref={iframeRef}
            src={`https://www.youtube.com/embed/${videoId}?autoplay=1&enablejsapi=1&origin=${window.location.origin}&rel=0&modestbranding=1`}
            title="YouTube Player"
            className="absolute inset-0 w-full h-full"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
          />
          <button
            onClick={onClose}
            className="absolute top-2 right-2 p-1.5 rounded-full bg-black/60 hover:bg-black/80 transition-colors"
            aria-label="Close player"
          >
            <X size={14} className="text-white" />
          </button>
        </div>
      </motion.div>
    </AnimatePresence>
  );
};
