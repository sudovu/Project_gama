import { useState, useCallback, useEffect } from 'react';

export interface ColorTheme {
  id: string;
  name: string;
  primary: string;
  primaryLight: string;
  accent: string;
  accentLight: string;
  gradient: string;
  glowColor: string;
}

export const colorThemes: ColorTheme[] = [
  {
    id: 'violet',
    name: 'Violet',
    primary: '#8b5cf6',
    primaryLight: '#a78bfa',
    accent: '#06b6d4',
    accentLight: '#22d3ee',
    gradient: 'linear-gradient(135deg, #8b5cf6, #06b6d4)',
    glowColor: 'rgba(139, 92, 246, 0.3)',
  },
  {
    id: 'aurora',
    name: 'Aurora',
    primary: '#06b6d4',
    primaryLight: '#22d3ee',
    accent: '#3b82f6',
    accentLight: '#60a5fa',
    gradient: 'linear-gradient(135deg, #06b6d4, #3b82f6)',
    glowColor: 'rgba(6, 182, 212, 0.3)',
  },
  {
    id: 'sunset',
    name: 'Sunset',
    primary: '#f97316',
    primaryLight: '#fb923c',
    accent: '#ec4899',
    accentLight: '#f472b6',
    gradient: 'linear-gradient(135deg, #f97316, #ec4899)',
    glowColor: 'rgba(249, 115, 22, 0.3)',
  },
  {
    id: 'emerald',
    name: 'Emerald',
    primary: '#10b981',
    primaryLight: '#34d399',
    accent: '#06b6d4',
    accentLight: '#22d3ee',
    gradient: 'linear-gradient(135deg, #10b981, #06b6d4)',
    glowColor: 'rgba(16, 185, 129, 0.3)',
  },
  {
    id: 'rose',
    name: 'Rose',
    primary: '#f43f5e',
    primaryLight: '#fb7185',
    accent: '#a855f7',
    accentLight: '#c084fc',
    gradient: 'linear-gradient(135deg, #f43f5e, #a855f7)',
    glowColor: 'rgba(244, 63, 94, 0.3)',
  },
  {
    id: 'midnight',
    name: 'Midnight',
    primary: '#6366f1',
    primaryLight: '#818cf8',
    accent: '#8b5cf6',
    accentLight: '#a78bfa',
    gradient: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
    glowColor: 'rgba(99, 102, 241, 0.3)',
  },
  {
    id: 'gold',
    name: 'Gold',
    primary: '#eab308',
    primaryLight: '#facc15',
    accent: '#f97316',
    accentLight: '#fb923c',
    gradient: 'linear-gradient(135deg, #eab308, #f97316)',
    glowColor: 'rgba(234, 179, 8, 0.3)',
  },
  {
    id: 'arctic',
    name: 'Arctic',
    primary: '#38bdf8',
    primaryLight: '#7dd3fc',
    accent: '#818cf8',
    accentLight: '#a5b4fc',
    gradient: 'linear-gradient(135deg, #38bdf8, #818cf8)',
    glowColor: 'rgba(56, 189, 248, 0.3)',
  },
];

export function useTheme() {
  const [theme, setTheme] = useState<ColorTheme>(colorThemes[0]);
  const [mode, setMode] = useState<'dark' | 'light'>('dark');

  // Load saved theme
  useEffect(() => {
    const savedTheme = localStorage.getItem('gamma-theme');
    const savedMode = localStorage.getItem('gamma-mode');
    
    if (savedTheme) {
      const found = colorThemes.find(t => t.id === savedTheme);
      if (found) setTheme(found);
    }
    if (savedMode === 'light' || savedMode === 'dark') {
      setMode(savedMode);
    }
  }, []);

  // Apply theme to DOM
  useEffect(() => {
    const root = document.documentElement;
    root.style.setProperty('--gamma-primary', theme.primary);
    root.style.setProperty('--gamma-primary-light', theme.primaryLight);
    root.style.setProperty('--gamma-accent', theme.accent);
    root.style.setProperty('--gamma-accent-light', theme.accentLight);
    root.style.setProperty('--gamma-gradient', theme.gradient);
    root.style.setProperty('--gamma-glow', theme.glowColor);
    
    root.classList.toggle('light', mode === 'light');
    
    localStorage.setItem('gamma-theme', theme.id);
    localStorage.setItem('gamma-mode', mode);
  }, [theme, mode]);

  const setColorTheme = useCallback((themeId: string) => {
    const found = colorThemes.find(t => t.id === themeId);
    if (found) setTheme(found);
  }, []);

  const toggleMode = useCallback(() => {
    setMode(prev => prev === 'dark' ? 'light' : 'dark');
  }, []);

  return {
    theme,
    mode,
    setColorTheme,
    toggleMode,
    setMode,
    themes: colorThemes,
  };
}
