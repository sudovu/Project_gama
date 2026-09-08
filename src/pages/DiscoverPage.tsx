import React from 'react';
import { Play, ArrowRight } from 'lucide-react';
import { motion } from 'framer-motion';
import { Track } from '../types';
import { GammaArtwork } from '../components/ui/GammaArtwork';
import { GammaSectionHeader } from '../components/ui/GammaSectionHeader';
import { moodCategories, trendingTracks, recentTracks, newReleases, artists, playlists } from '../data/mockData';

interface DiscoverPageProps {
  onPlayTrack: (track: Track, queue?: Track[]) => void;
  onNavigateToArtist: (artistId: string) => void;
  onNavigateToAlbum: (albumId: string) => void;
  onNavigateToPlaylist: (playlistId: string) => void;
}

const getGreeting = () => {
  const hour = new Date().getHours();
  if (hour < 6) return 'Late night session';
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
};

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: { staggerChildren: 0.05 }
  }
};

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.3 } }
};

export const DiscoverPage: React.FC<DiscoverPageProps> = ({ onPlayTrack, onNavigateToArtist, onNavigateToAlbum, onNavigateToPlaylist }) => {
  return (
    <motion.div 
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      className="pb-32 px-4 md:px-6 lg:px-8"
    >
      {/* Header / Greeting */}
      <motion.div variants={itemVariants} className="pt-6 pb-4">
        <div className="flex items-center gap-2 mb-1">
          <div className="w-2 h-2 rounded-full bg-gamma-primary animate-pulse" />
          <span className="text-xs text-gamma-primary font-medium uppercase tracking-wider">Now Resonating</span>
        </div>
        <h1 className="text-3xl md:text-4xl font-display font-bold text-gamma-text-primary">
          {getGreeting()}<span className="text-gamma-primary">.</span>
        </h1>
        <p className="text-gamma-text-secondary mt-2 text-sm md:text-base">
          What's your frequency today?
        </p>
      </motion.div>

      {/* Featured Banner */}
      <motion.div variants={itemVariants} className="mb-8">
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-gamma-primary/30 via-gamma-surface-elevated to-gamma-accent/20 border border-gamma-border/50 p-6">
          <div className="absolute top-0 right-0 w-40 h-40 bg-gamma-primary/10 rounded-full blur-3xl" />
          <div className="absolute bottom-0 left-0 w-32 h-32 bg-gamma-accent/10 rounded-full blur-3xl" />
          <div className="relative z-10">
            <p className="text-xs text-gamma-primary font-medium uppercase tracking-wider mb-2">Featured Frequency</p>
            <h3 className="text-xl font-display font-bold text-gamma-text-primary mb-1">Gamma Frequency</h3>
            <p className="text-sm text-gamma-text-secondary mb-4">The essential GAMMA experience — curated signals for your ears</p>
            <button
              onClick={() => onPlayTrack(playlists[0].tracks[0], playlists[0].tracks)}
              className="flex items-center gap-2 px-5 py-2.5 rounded-full gradient-primary text-white text-sm font-medium hover:opacity-90 transition-opacity shadow-lg shadow-gamma-primary/20"
            >
              <Play size={16} className="fill-white" />
              Play Now
            </button>
          </div>
        </div>
      </motion.div>

      {/* Mood Explorer */}
      <motion.div variants={itemVariants} className="mb-8">
        <GammaSectionHeader title="Mood Explorer" subtitle="Find your signal" />
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {moodCategories.map((mood) => (
            <motion.button
              key={mood.id}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className={`relative overflow-hidden rounded-2xl p-4 bg-gradient-to-br ${mood.gradient} text-left transition-shadow hover:shadow-lg`}
            >
              <span className="text-2xl mb-2 block">{mood.icon}</span>
              <span className="text-sm font-medium text-white">{mood.name}</span>
            </motion.button>
          ))}
        </div>
      </motion.div>

      {/* Continue Listening */}
      <motion.div variants={itemVariants} className="mb-8">
        <GammaSectionHeader 
          title="Continue Listening" 
          action={
            <button className="text-xs text-gamma-primary flex items-center gap-1 hover:text-gamma-primary-light transition-colors">
              See all <ArrowRight size={12} />
            </button>
          }
        />
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
          {recentTracks.map((track) => (
            <motion.button
              key={track.id}
              whileHover={{ scale: 1.01 }}
              whileTap={{ scale: 0.99 }}
              onClick={() => onPlayTrack(track, recentTracks)}
              className="flex items-center gap-3 p-2 rounded-xl bg-gamma-surface hover:bg-gamma-surface-elevated transition-colors text-left"
            >
              <GammaArtwork seed={track.id} type="track" size="sm" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gamma-text-primary truncate">{track.title}</p>
                <p className="text-xs text-gamma-text-secondary truncate">{track.artist}</p>
              </div>
              <div className="p-2 rounded-full bg-gamma-primary/20">
                <Play size={12} className="text-gamma-primary fill-gamma-primary" />
              </div>
            </motion.button>
          ))}
        </div>
      </motion.div>

      {/* Trending */}
      <motion.div variants={itemVariants} className="mb-8">
        <GammaSectionHeader 
          title="Trending Now" 
          subtitle="Signals rising across the network"
        />
        <div className="space-y-1">
          {trendingTracks.map((track, index) => (
            <motion.button
              key={track.id}
              whileHover={{ x: 4 }}
              onClick={() => onPlayTrack(track, trendingTracks)}
              className="flex items-center gap-3 w-full p-2 rounded-xl hover:bg-gamma-surface-hover transition-colors text-left"
            >
              <span className="w-6 text-center text-sm font-medium text-gamma-text-muted">{index + 1}</span>
              <GammaArtwork seed={track.id} type="track" size="sm" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gamma-text-primary truncate">{track.title}</p>
                <p className="text-xs text-gamma-text-secondary truncate">{track.artist}</p>
              </div>
              <span className="text-xs text-gamma-text-muted">{track.genre}</span>
            </motion.button>
          ))}
        </div>
      </motion.div>

      {/* New Releases */}
      <motion.div variants={itemVariants} className="mb-8">
        <GammaSectionHeader 
          title="New Releases"
          action={
            <button className="text-xs text-gamma-primary flex items-center gap-1 hover:text-gamma-primary-light transition-colors">
              See all <ArrowRight size={12} />
            </button>
          }
        />
        <div className="flex gap-4 overflow-x-auto pb-2 -mx-4 px-4 scrollbar-hide">
          {newReleases.map((album) => (
            <motion.button
              key={album.id}
              whileHover={{ scale: 1.03 }}
              whileTap={{ scale: 0.97 }}
              onClick={() => onNavigateToAlbum(album.id)}
              className="flex-shrink-0 w-36 text-left"
            >
              <GammaArtwork seed={album.id} type="album" size="lg" className="!w-36 !h-36 mb-2" />
              <p className="text-sm font-medium text-gamma-text-primary truncate">{album.title}</p>
              <p className="text-xs text-gamma-text-secondary truncate">{album.artist}</p>
            </motion.button>
          ))}
        </div>
      </motion.div>

      {/* Featured Artists */}
      <motion.div variants={itemVariants} className="mb-8">
        <GammaSectionHeader 
          title="Discover Artists"
          action={
            <button className="text-xs text-gamma-primary flex items-center gap-1 hover:text-gamma-primary-light transition-colors">
              See all <ArrowRight size={12} />
            </button>
          }
        />
        <div className="flex gap-4 overflow-x-auto pb-2 -mx-4 px-4 scrollbar-hide">
          {artists.slice(0, 6).map((artist) => (
            <motion.button
              key={artist.id}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => onNavigateToArtist(artist.id)}
              className="flex-shrink-0 w-28 text-center"
            >
              <GammaArtwork seed={artist.id} type="artist" size="lg" className="!w-24 !h-24 mx-auto mb-2 rounded-full" rounded />
              <p className="text-sm font-medium text-gamma-text-primary truncate">{artist.name}</p>
              <p className="text-xs text-gamma-text-muted">{artist.genre}</p>
            </motion.button>
          ))}
        </div>
      </motion.div>

      {/* Curated Playlists */}
      <motion.div variants={itemVariants} className="mb-8">
        <GammaSectionHeader 
          title="Curated Frequencies" 
          subtitle="Hand-tuned for your ears"
          action={
            <button className="text-xs text-gamma-primary flex items-center gap-1 hover:text-gamma-primary-light transition-colors">
              See all <ArrowRight size={12} />
            </button>
          }
        />
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
          {playlists.slice(0, 6).map((playlist) => (
            <motion.button
              key={playlist.id}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={() => onNavigateToPlaylist(playlist.id)}
              className="text-left group"
            >
              <div className="relative overflow-hidden rounded-2xl mb-2">
                <GammaArtwork seed={playlist.id} type="playlist" size="lg" className="!w-full !h-32" />
                <div className="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-colors flex items-center justify-center">
                  <div className="opacity-0 group-hover:opacity-100 transition-opacity p-3 rounded-full gradient-primary shadow-lg">
                    <Play size={20} className="text-white fill-white" />
                  </div>
                </div>
              </div>
              <p className="text-sm font-medium text-gamma-text-primary truncate">{playlist.title}</p>
              <p className="text-xs text-gamma-text-secondary truncate">{playlist.tracks.length} tracks</p>
            </motion.button>
          ))}
        </div>
      </motion.div>
    </motion.div>
  );
};
