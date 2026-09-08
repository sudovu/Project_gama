import React from 'react';
import { Moon, Sun, Volume2, Wifi, Shield, Info, ChevronRight, Check, Mail, Phone, User, Code2, Sparkles } from 'lucide-react';
import { motion } from 'framer-motion';
import { ColorTheme, colorThemes } from '../hooks/useTheme';

interface ProfilePageProps {
  theme: ColorTheme;
  mode: 'dark' | 'light';
  onToggleTheme: () => void;
  onSelectColor: (themeId: string) => void;
  totalFavorites: number;
  totalPlaylists: number;
  totalRecent: number;
}

export const ProfilePage: React.FC<ProfilePageProps> = ({
  theme,
  mode,
  onToggleTheme,
  onSelectColor,
  totalFavorites,
  totalPlaylists,
  totalRecent,
}) => {
  return (
    <div className="pb-32 px-4 md:px-6 lg:px-8">
      {/* Profile Header */}
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="pt-6 pb-8"
      >
        <div className="flex items-center gap-4">
          <div 
            className="w-16 h-16 rounded-full flex items-center justify-center"
            style={{ background: theme.gradient }}
          >
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
          <p className="text-2xl font-display font-bold" style={{ color: theme.primary }}>{totalFavorites}</p>
          <p className="text-xs text-gamma-text-secondary mt-1">Favorites</p>
        </div>
        <div className="p-4 rounded-2xl bg-gamma-surface-elevated border border-gamma-border text-center">
          <p className="text-2xl font-display font-bold" style={{ color: theme.accent }}>{totalPlaylists}</p>
          <p className="text-xs text-gamma-text-secondary mt-1">Playlists</p>
        </div>
        <div className="p-4 rounded-2xl bg-gamma-surface-elevated border border-gamma-border text-center">
          <p className="text-2xl font-display font-bold text-gamma-text-primary">{totalRecent}</p>
          <p className="text-xs text-gamma-text-secondary mt-1">Recent</p>
        </div>
      </motion.div>

      {/* Color Theme Picker */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.15 }}
        className="mb-6"
      >
        <h3 className="text-xs font-medium text-gamma-text-muted uppercase tracking-wider mb-3 px-1">
          Color Theme
        </h3>
        <div className="rounded-2xl bg-gamma-surface-elevated border border-gamma-border p-4">
          <div className="grid grid-cols-4 gap-3">
            {colorThemes.map((colorTheme) => (
              <motion.button
                key={colorTheme.id}
                whileTap={{ scale: 0.95 }}
                onClick={() => onSelectColor(colorTheme.id)}
                className={`relative flex flex-col items-center gap-2 p-3 rounded-xl transition-all ${
                  theme.id === colorTheme.id 
                    ? 'bg-gamma-surface-hover ring-2 ring-offset-2 ring-offset-gamma-surface-elevated' 
                    : 'hover:bg-gamma-surface-hover'
                }`}
                style={{ 
                  outlineColor: theme.id === colorTheme.id ? colorTheme.primary : undefined,
                  outline: theme.id === colorTheme.id ? `2px solid ${colorTheme.primary}` : undefined,
                  outlineOffset: '2px',
                }}
                aria-label={`Select ${colorTheme.name} theme`}
              >
                {/* Color swatch */}
                <div 
                  className="w-10 h-10 rounded-full shadow-lg"
                  style={{ background: colorTheme.gradient }}
                />
                {theme.id === colorTheme.id && (
                  <div 
                    className="absolute -top-1 -right-1 w-5 h-5 rounded-full flex items-center justify-center"
                    style={{ background: colorTheme.primary }}
                  >
                    <Check size={12} className="text-white" />
                  </div>
                )}
                <span className="text-[10px] font-medium text-gamma-text-secondary">
                  {colorTheme.name}
                </span>
              </motion.button>
            ))}
          </div>
        </div>
      </motion.div>

      {/* Settings Groups */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="mb-6"
      >
        <h3 className="text-xs font-medium text-gamma-text-muted uppercase tracking-wider mb-3 px-1">
          Appearance
        </h3>
        <div className="rounded-2xl bg-gamma-surface-elevated border border-gamma-border overflow-hidden">
          <button
            onClick={onToggleTheme}
            className="flex items-center gap-3 w-full p-4 hover:bg-gamma-surface-hover transition-colors text-left"
          >
            {mode === 'dark' ? (
              <Moon size={18} className="text-gamma-text-secondary flex-shrink-0" />
            ) : (
              <Sun size={18} className="text-gamma-text-secondary flex-shrink-0" />
            )}
            <span className="flex-1 text-sm text-gamma-text-primary">Theme Mode</span>
            <span className="text-sm text-gamma-text-secondary">
              {mode === 'dark' ? 'Dark' : 'Light'}
            </span>
            <ChevronRight size={16} className="text-gamma-text-muted" />
          </button>
        </div>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.25 }}
        className="mb-6"
      >
        <h3 className="text-xs font-medium text-gamma-text-muted uppercase tracking-wider mb-3 px-1">
          Playback
        </h3>
        <div className="rounded-2xl bg-gamma-surface-elevated border border-gamma-border overflow-hidden">
          <div className="flex items-center gap-3 w-full p-4 text-left">
            <Volume2 size={18} className="text-gamma-text-secondary flex-shrink-0" />
            <span className="flex-1 text-sm text-gamma-text-primary">Audio Quality</span>
            <span className="text-sm text-gamma-text-secondary">High</span>
            <ChevronRight size={16} className="text-gamma-text-muted" />
          </div>
          <div className="border-t border-gamma-border/50 flex items-center gap-3 w-full p-4 text-left">
            <Wifi size={18} className="text-gamma-text-secondary flex-shrink-0" />
            <span className="flex-1 text-sm text-gamma-text-primary">Stream Source</span>
            <span className="text-sm text-gamma-success flex items-center gap-1">
              <span className="w-2 h-2 rounded-full bg-gamma-success" />
              YouTube
            </span>
          </div>
        </div>
      </motion.div>

      {/* Developer Profile Section */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
        className="mb-6"
      >
        <h3 className="text-xs font-medium text-gamma-text-muted uppercase tracking-wider mb-3 px-1 flex items-center gap-2">
          <Code2 size={14} />
          Developed By
        </h3>
        <div className="rounded-2xl bg-gradient-to-br from-gamma-surface-elevated to-gamma-surface border border-gamma-border overflow-hidden">
          {/* Developer Header with Gradient */}
          <div className="relative p-6 pb-4">
            <div 
              className="absolute inset-0 opacity-10"
              style={{ background: theme.gradient }}
            />
            <div className="relative flex flex-col items-center text-center">
              {/* Profile Picture */}
              <div className="relative mb-4">
                <div 
                  className="w-24 h-24 rounded-full p-1"
                  style={{ background: theme.gradient }}
                >
                  <div className="w-full h-full rounded-full bg-gamma-surface-elevated flex items-center justify-center overflow-hidden">
                    <div 
                      className="w-full h-full flex items-center justify-center text-3xl font-bold"
                      style={{ 
                        background: theme.gradient,
                        color: 'white'
                      }}
                    >
                      BG
                    </div>
                  </div>
                </div>
                <div className="absolute -bottom-1 -right-1 w-8 h-8 rounded-full bg-gamma-success flex items-center justify-center border-2 border-gamma-surface-elevated">
                  <Check size={16} className="text-white" />
                </div>
              </div>

              {/* Developer Name */}
              <h4 className="text-2xl font-display font-bold text-gamma-text-primary mb-1">
                Bhuwan Gautam
              </h4>
              <div className="flex items-center gap-2 mb-3">
                <Sparkles size={14} style={{ color: theme.primary }} />
                <p className="text-sm font-medium" style={{ color: theme.primary }}>
                  Full Stack Developer
                </p>
              </div>
              <p className="text-xs text-gamma-text-secondary max-w-xs">
                Building the future of music discovery with GAMMA
              </p>
            </div>
          </div>

          {/* Contact Information */}
          <div className="border-t border-gamma-border/50">
            {/* Email */}
            <a 
              href="mailto:vhuwonmathers@gmail.com"
              className="flex items-center gap-3 w-full p-4 text-left hover:bg-gamma-surface-hover transition-colors"
            >
              <div 
                className="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0"
                style={{ background: `${theme.primary}20` }}
              >
                <Mail size={18} style={{ color: theme.primary }} />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-xs text-gamma-text-muted mb-0.5">Email</p>
                <p className="text-sm text-gamma-text-primary font-medium truncate">
                  vhuwonmathers@gmail.com
                </p>
              </div>
              <ChevronRight size={16} className="text-gamma-text-muted" />
            </a>

            {/* Username */}
            <div className="border-t border-gamma-border/50 flex items-center gap-3 w-full p-4 text-left">
              <div 
                className="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0"
                style={{ background: `${theme.primary}20` }}
              >
                <User size={18} style={{ color: theme.primary }} />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-xs text-gamma-text-muted mb-0.5">Username</p>
                <p className="text-sm text-gamma-text-primary font-medium">
                  @sudovu
                </p>
              </div>
            </div>

            {/* Phone */}
            <a 
              href="tel:9869367788"
              className="border-t border-gamma-border/50 flex items-center gap-3 w-full p-4 text-left hover:bg-gamma-surface-hover transition-colors"
            >
              <div 
                className="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0"
                style={{ background: `${theme.primary}20` }}
              >
                <Phone size={18} style={{ color: theme.primary }} />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-xs text-gamma-text-muted mb-0.5">Phone</p>
                <p className="text-sm text-gamma-text-primary font-medium">
                  +977 9869367788
                </p>
              </div>
              <ChevronRight size={16} className="text-gamma-text-muted" />
            </a>
          </div>

          {/* App Info Footer */}
          <div className="border-t border-gamma-border/50 bg-gamma-surface/50 p-4">
            <div className="flex items-center justify-between text-xs">
              <div className="flex items-center gap-2">
                <Info size={14} className="text-gamma-text-muted" />
                <span className="text-gamma-text-secondary">GAMMA v1.0.0</span>
              </div>
              <div className="flex items-center gap-1">
                <span className="text-gamma-text-muted">Made with</span>
                <span className="text-red-500">♥</span>
              </div>
            </div>
          </div>
        </div>
      </motion.div>

      {/* Footer */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.35 }}
        className="text-center py-8"
      >
        <p className="text-xs text-gamma-text-muted">
          GAMMA v1.0.0 — Your Personal Frequency
        </p>
        <p className="text-xs text-gamma-text-muted mt-1">
          Powered by YouTube
        </p>
      </motion.div>
    </div>
  );
};
