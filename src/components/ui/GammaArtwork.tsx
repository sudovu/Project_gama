import React from 'react';
import { Music, User, Disc, ListMusic } from 'lucide-react';

interface GammaArtworkProps {
  seed?: string;
  type?: 'track' | 'artist' | 'album' | 'playlist';
  size?: 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
  rounded?: boolean;
}

const gradients = [
  'from-violet-600 via-purple-600 to-indigo-800',
  'from-cyan-500 via-blue-500 to-indigo-700',
  'from-fuchsia-500 via-pink-500 to-purple-700',
  'from-emerald-500 via-teal-500 to-cyan-700',
  'from-amber-500 via-orange-500 to-red-700',
  'from-rose-500 via-pink-500 to-fuchsia-700',
  'from-sky-500 via-blue-500 to-violet-700',
  'from-lime-500 via-green-500 to-emerald-700',
];

const sizeClasses = {
  sm: 'w-10 h-10',
  md: 'w-12 h-12',
  lg: 'w-32 h-32',
  xl: 'w-56 h-56',
};

const iconSizes = {
  sm: 14,
  md: 18,
  lg: 40,
  xl: 64,
};

export const GammaArtwork: React.FC<GammaArtworkProps> = ({ 
  seed = 'default', 
  type = 'track', 
  size = 'md',
  className = '',
  rounded = false,
}) => {
  const hash = seed.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
  const gradient = gradients[hash % gradients.length];
  
  const Icon = type === 'artist' ? User : type === 'album' ? Disc : type === 'playlist' ? ListMusic : Music;

  return (
    <div 
      className={`${sizeClasses[size]} bg-gradient-to-br ${gradient} flex items-center justify-center flex-shrink-0 ${rounded ? 'rounded-full' : 'rounded-xl'} ${className} shadow-lg`}
    >
      <Icon size={iconSizes[size]} className="text-white/80" />
    </div>
  );
};
