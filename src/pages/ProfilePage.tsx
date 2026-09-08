import React from 'react';
import { Moon, Sun, Volume2, Wifi, Shield, Info, ChevronRight } from 'lucide-react';
import { motion } from 'framer-motion';

interface ProfilePageProps {
  theme: 'dark' | 'light';
  onToggleTheme: () => void;
}

export const ProfilePage: React.FC<ProfilePageProps> = ({ theme, onToggleTheme }) => {
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

  return (
    <div className="pb-32 px-4 md:px-6 lg:px-8">
      {/* Profile Header */}
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="pt-6 pb-8"
      >
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-full gradient-primary flex items-center justify-center">
            <span className="text-2xl font-display font-bold text-white">G</span>
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
        className="grid grid-cols-3 gap-3 mb-8"
      >
        <div className="p-4 rounded-2xl bg-gamma-surface-elevated border border-gamma-border text-center">
          <p className="text-2xl font-display font-bold text-gamma-primary">4</p>
          <p className="text-xs text-gamma-text-secondary mt-1">Favorites</p>
        </div>
        <div className="p-4 rounded-2xl bg-gamma-surface-elevated border border-gamma-border text-center">
          <p className="text-2xl font-display font-bold text-gamma-accent">6</p>
          <p className="text-xs text-gamma-text-secondary mt-1">Playlists</p>
        </div>
        <div className="p-4 rounded-2xl bg-gamma-surface-elevated border border-gamma-border text-center">
          <p className="text-2xl font-display font-bold text-gamma-text-primary">∞</p>
          <p className="text-xs text-gamma-text-secondary mt-1">Frequency</p>
        </div>
      </motion.div>

      {/* Settings Groups */}
      {settingsGroups.map((group, groupIndex) => (
        <motion.div
          key={group.title}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.15 + groupIndex * 0.05 }}
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
        transition={{ delay: 0.3 }}
        className="text-center py-8"
      >
        <p className="text-xs text-gamma-text-muted">
          GAMMA v1.0.0 — Your Personal Frequency
        </p>
        <p className="text-xs text-gamma-text-muted mt-1">
          Built with passion for music discovery
        </p>
      </motion.div>
    </div>
  );
};
