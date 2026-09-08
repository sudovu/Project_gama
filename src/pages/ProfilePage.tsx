import React from 'react';
import { Moon, Sun, Volume2, Wifi, Shield, Info, ChevronRight, Music, Clock, Radio, Heart } from 'lucide-react';
import { motion } from 'framer-motion';

interface ProfilePageProps {
  theme: 'dark' | 'light';
  onToggleTheme: () => void;
  totalFavorites: number;
  totalPlaylists: number;
  totalRecent: number;
}

export const ProfilePage: React.FC<ProfilePageProps> = ({ theme, onToggleTheme, totalFavorites, totalPlaylists, totalRecent }) => {
  interface SettingsItem {
    icon: typeof Volume2;
    label: string;
    value?: string | boolean;
    action?: boolean;
    onClick?: () => void;
  }

  interface SettingsGroup {
    title: string;
    items: SettingsItem[];
  }

  const settingsGroups: SettingsGroup[] = [
    {
      title: 'Playback',
      items: [
        { icon: Volume2, label: 'Audio Quality', value: 'High', action: true },
        { icon: Wifi, label: 'Stream over Wi-Fi only', value: false, action: true },
      ]
    },
    {
      title: 'Appearance',
      items: [
        { icon: theme === 'dark' ? Moon : Sun, label: 'Theme', value: theme === 'dark' ? 'Dark' : 'Light', action: true, onClick: onToggleTheme },
      ]
    },
    {
      title: 'About',
      items: [
        { icon: Shield, label: 'Privacy Policy', action: true },
        { icon: Info, label: 'About GAMMA', value: 'v1.0.0', action: true },
      ]
    },
  ];

  // Mock listening data for the week
  const weeklyData = [35, 52, 28, 65, 48, 72, 58];
  const maxVal = Math.max(...weeklyData);
  const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  return (
    <div className="pb-32 px-4 md:px-6 lg:px-8">
      {/* Profile Header */}
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="pt-6 pb-6"
      >
        <div className="flex items-center gap-4">
          <div className="relative">
            <div className="w-16 h-16 rounded-full gradient-primary flex items-center justify-center">
              <span className="text-2xl font-display font-bold text-white">G</span>
            </div>
            <div className="absolute -bottom-0.5 -right-0.5 w-5 h-5 rounded-full bg-gamma-success border-2 border-gamma-bg" />
          </div>
          <div>
            <h1 className="text-2xl font-display font-bold text-gamma-text-primary">
              GAMMA User
            </h1>
            <p className="text-sm text-gamma-text-secondary">Your personal frequency</p>
          </div>
        </div>
      </motion.div>

      {/* Stats */}
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="grid grid-cols-3 gap-3 mb-6"
      >
        <div className="p-4 rounded-2xl bg-gamma-surface-elevated border border-gamma-border text-center">
          <Heart className="mx-auto mb-2 text-gamma-primary" size={18} />
          <p className="text-2xl font-display font-bold text-gamma-primary">{totalFavorites}</p>
          <p className="text-xs text-gamma-text-secondary mt-1">Favorites</p>
        </div>
        <div className="p-4 rounded-2xl bg-gamma-surface-elevated border border-gamma-border text-center">
          <Music className="mx-auto mb-2 text-gamma-accent" size={18} />
          <p className="text-2xl font-display font-bold text-gamma-accent">{totalPlaylists}</p>
          <p className="text-xs text-gamma-text-secondary mt-1">Playlists</p>
        </div>
        <div className="p-4 rounded-2xl bg-gamma-surface-elevated border border-gamma-border text-center">
          <Clock className="mx-auto mb-2 text-gamma-text-primary" size={18} />
          <p className="text-2xl font-display font-bold text-gamma-text-primary">{totalRecent}</p>
          <p className="text-xs text-gamma-text-secondary mt-1">Recent</p>
        </div>
      </motion.div>

      {/* Listening Activity Chart */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.15 }}
        className="mb-8 p-5 rounded-2xl bg-gamma-surface-elevated border border-gamma-border"
      >
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Radio size={16} className="text-gamma-primary" />
            <h3 className="text-sm font-medium text-gamma-text-primary">This Week's Frequency</h3>
          </div>
          <span className="text-xs text-gamma-text-muted">45h 18m total</span>
        </div>
        <div className="flex items-end justify-between gap-2 h-24">
          {weeklyData.map((val, i) => (
            <div key={i} className="flex-1 flex flex-col items-center gap-1.5">
              <motion.div
                initial={{ height: 0 }}
                animate={{ height: `${(val / maxVal) * 100}%` }}
                transition={{ delay: 0.2 + i * 0.05, duration: 0.5 }}
                className="w-full rounded-t-md bg-gradient-to-t from-gamma-primary/40 to-gamma-primary"
                style={{ minHeight: '4px' }}
              />
              <span className="text-[10px] text-gamma-text-muted">{days[i]}</span>
            </div>
          ))}
        </div>
      </motion.div>

      {/* Top Genres */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="mb-8"
      >
        <h3 className="text-xs font-medium text-gamma-text-muted uppercase tracking-wider mb-3 px-1">
          Your Top Genres
        </h3>
        <div className="flex flex-wrap gap-2">
          {['Electronic', 'Ambient', 'Synthwave', 'Deep House', 'Techno'].map((genre, i) => (
            <span 
              key={genre}
              className="px-3 py-1.5 rounded-full text-xs font-medium bg-gamma-surface-elevated border border-gamma-border text-gamma-text-secondary"
            >
              {genre}
            </span>
          ))}
        </div>
      </motion.div>

      {/* Settings Groups */}
      {settingsGroups.map((group, groupIndex) => (
        <motion.div
          key={group.title}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.25 + groupIndex * 0.05 }}
          className="mb-6"
        >
          <h3 className="text-xs font-medium text-gamma-text-muted uppercase tracking-wider mb-3 px-1">
            {group.title}
          </h3>
          <div className="rounded-2xl bg-gamma-surface-elevated border border-gamma-border overflow-hidden">
            {group.items.map((item, index) => (
              <button
                key={item.label}
                onClick={item.onClick}
                className={`flex items-center gap-3 w-full p-4 hover:bg-gamma-surface-hover transition-colors text-left ${
                  index > 0 ? 'border-t border-gamma-border/50' : ''
                }`}
              >
                <item.icon size={18} className="text-gamma-text-secondary flex-shrink-0" />
                <span className="flex-1 text-sm text-gamma-text-primary">{item.label}</span>
                {item.value !== undefined && (
                  <span className="text-sm text-gamma-text-secondary">
                    {typeof item.value === 'boolean' ? (item.value ? 'On' : 'Off') : item.value}
                  </span>
                )}
                {item.action && <ChevronRight size={16} className="text-gamma-text-muted" />}
              </button>
            ))}
          </div>
        </motion.div>
      ))}

      {/* Footer */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.4 }}
        className="text-center py-8"
      >
        <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-gamma-surface-elevated border border-gamma-border mb-3">
          <div className="w-2 h-2 rounded-full gradient-primary" />
          <p className="text-xs text-gamma-text-muted font-medium">
            GAMMA v1.0.0
          </p>
        </div>
        <p className="text-xs text-gamma-text-muted">
          Your Personal Frequency — Discover, Explore, Experience
        </p>
      </motion.div>
    </div>
  );
};


