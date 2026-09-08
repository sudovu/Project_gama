import { useState, useCallback, useEffect, useRef } from 'react';
import { Track, PlayerState, PlaybackState, RepeatMode } from '../types';
import { tracks as allTracks } from '../data/mockData';

const initialState: PlayerState = {
  currentTrack: null,
  queue: [],
  queueIndex: -1,
  playbackState: 'idle',
  progress: 0,
  volume: 75,
  isShuffled: false,
  repeatMode: 'off',
  isExpanded: false,
};

export function usePlayer() {
  const [state, setState] = useState<PlayerState>(initialState);
  const progressInterval = useRef<ReturnType<typeof setInterval> | null>(null);

  const startProgressSimulation = useCallback(() => {
    if (progressInterval.current) clearInterval(progressInterval.current);
    progressInterval.current = setInterval(() => {
      setState(prev => {
        if (prev.playbackState !== 'playing' || !prev.currentTrack) return prev;
        const newProgress = prev.progress + 1;
        if (newProgress >= prev.currentTrack.duration) {
          // Track ended
          return prev;
        }
        return { ...prev, progress: newProgress };
      });
    }, 1000);
  }, []);

  const stopProgressSimulation = useCallback(() => {
    if (progressInterval.current) {
      clearInterval(progressInterval.current);
      progressInterval.current = null;
    }
  }, []);

  useEffect(() => {
    if (state.playbackState === 'playing') {
      startProgressSimulation();
    } else {
      stopProgressSimulation();
    }
    return () => stopProgressSimulation();
  }, [state.playbackState, startProgressSimulation, stopProgressSimulation]);

  // Handle track end
  useEffect(() => {
    if (state.currentTrack && state.progress >= state.currentTrack.duration) {
      handleNext();
    }
  }, [state.progress, state.currentTrack]);

  const playTrack = useCallback((track: Track, queue?: Track[]) => {
    const newQueue = queue || [track];
    const index = newQueue.findIndex(t => t.id === track.id);
    setState(prev => ({
      ...prev,
      currentTrack: track,
      queue: newQueue,
      queueIndex: index >= 0 ? index : 0,
      playbackState: 'playing',
      progress: 0,
    }));
  }, []);

  const playQueue = useCallback((queue: Track[], startIndex: number = 0) => {
    if (queue.length === 0) return;
    setState(prev => ({
      ...prev,
      currentTrack: queue[startIndex],
      queue,
      queueIndex: startIndex,
      playbackState: 'playing',
      progress: 0,
    }));
  }, []);

  const togglePlay = useCallback(() => {
    setState(prev => ({
      ...prev,
      playbackState: prev.playbackState === 'playing' ? 'paused' : 'playing',
    }));
  }, []);

  const handleNext = useCallback(() => {
    setState(prev => {
      if (prev.queue.length === 0) return prev;
      
      if (prev.repeatMode === 'one') {
        return { ...prev, progress: 0, playbackState: 'playing' };
      }

      let nextIndex: number;
      if (prev.isShuffled) {
        nextIndex = Math.floor(Math.random() * prev.queue.length);
      } else {
        nextIndex = prev.queueIndex + 1;
      }

      if (nextIndex >= prev.queue.length) {
        if (prev.repeatMode === 'all') {
          nextIndex = 0;
        } else {
          return { ...prev, playbackState: 'idle' };
        }
      }

      return {
        ...prev,
        currentTrack: prev.queue[nextIndex],
        queueIndex: nextIndex,
        progress: 0,
        playbackState: 'playing',
      };
    });
  }, []);

  const handlePrevious = useCallback(() => {
    setState(prev => {
      if (prev.progress > 3) {
        return { ...prev, progress: 0 };
      }
      if (prev.queue.length === 0) return prev;
      
      let prevIndex = prev.queueIndex - 1;
      if (prevIndex < 0) {
        prevIndex = prev.repeatMode === 'all' ? prev.queue.length - 1 : 0;
      }

      return {
        ...prev,
        currentTrack: prev.queue[prevIndex],
        queueIndex: prevIndex,
        progress: 0,
        playbackState: 'playing',
      };
    });
  }, []);

  const seek = useCallback((progress: number) => {
    setState(prev => ({ ...prev, progress }));
  }, []);

  const setVolume = useCallback((volume: number) => {
    setState(prev => ({ ...prev, volume }));
  }, []);

  const toggleShuffle = useCallback(() => {
    setState(prev => ({ ...prev, isShuffled: !prev.isShuffled }));
  }, []);

  const cycleRepeat = useCallback(() => {
    setState(prev => {
      const modes: RepeatMode[] = ['off', 'all', 'one'];
      const currentIndex = modes.indexOf(prev.repeatMode);
      return { ...prev, repeatMode: modes[(currentIndex + 1) % modes.length] };
    });
  }, []);

  const toggleExpanded = useCallback(() => {
    setState(prev => ({ ...prev, isExpanded: !prev.isExpanded }));
  }, []);

  const addToQueue = useCallback((track: Track) => {
    setState(prev => ({
      ...prev,
      queue: [...prev.queue, track],
    }));
  }, []);

  const removeFromQueue = useCallback((index: number) => {
    setState(prev => {
      const newQueue = prev.queue.filter((_, i) => i !== index);
      let newIndex = prev.queueIndex;
      if (index < prev.queueIndex) newIndex--;
      if (index === prev.queueIndex) {
        return {
          ...prev,
          queue: newQueue,
          queueIndex: Math.min(newIndex, newQueue.length - 1),
          currentTrack: newQueue[Math.min(newIndex, newQueue.length - 1)] || null,
          playbackState: newQueue.length === 0 ? 'idle' : prev.playbackState,
        };
      }
      return { ...prev, queue: newQueue, queueIndex: newIndex };
    });
  }, []);

  const clearQueue = useCallback(() => {
    setState(prev => ({
      ...prev,
      queue: prev.currentTrack ? [prev.currentTrack] : [],
      queueIndex: prev.currentTrack ? 0 : -1,
    }));
  }, []);

  return {
    state,
    playTrack,
    playQueue,
    togglePlay,
    handleNext,
    handlePrevious,
    seek,
    setVolume,
    toggleShuffle,
    cycleRepeat,
    toggleExpanded,
    addToQueue,
    removeFromQueue,
    clearQueue,
  };
}
