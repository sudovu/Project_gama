import { Track, Artist, Album, Playlist, MoodCategory } from '../types';

const artworkColors = [
  'from-violet-600 to-indigo-900',
  'from-cyan-500 to-blue-900',
  'from-fuchsia-600 to-purple-900',
  'from-emerald-500 to-teal-900',
  'from-amber-500 to-orange-900',
  'from-rose-500 to-pink-900',
  'from-sky-500 to-indigo-900',
  'from-lime-500 to-green-900',
];

export const generateArtworkUrl = (seed: string, index: number): string => {
  const color = artworkColors[index % artworkColors.length];
  return `gradient:${seed}:${color}`;
};

export const artists: Artist[] = [
  { id: 'a1', name: 'Luna Eclipse', artwork: '', genre: 'Electronic', followers: '2.4M', description: 'Ambient electronic producer crafting ethereal soundscapes.' },
  { id: 'a2', name: 'Neon Drift', artwork: '', genre: 'Synthwave', followers: '1.8M', description: 'Retro-futuristic synthwave collective.' },
  { id: 'a3', name: 'Void Walker', artwork: '', genre: 'Dark Ambient', followers: '890K', description: 'Dark ambient explorations of cosmic space.' },
  { id: 'a4', name: 'Crystal Frequencies', artwork: '', genre: 'Ambient', followers: '1.2M', description: 'Meditative ambient compositions.' },
  { id: 'a5', name: 'Phantom Circuit', artwork: '', genre: 'Techno', followers: '3.1M', description: 'Berlin-based techno innovator.' },
  { id: 'a6', name: 'Solar Flare', artwork: '', genre: 'Indie Electronic', followers: '670K', description: 'Indie electronic with organic textures.' },
  { id: 'a7', name: 'Deep Current', artwork: '', genre: 'Deep House', followers: '1.5M', description: 'Deep house grooves for late nights.' },
  { id: 'a8', name: 'Arctic Signal', artwork: '', genre: 'Post-Rock', followers: '420K', description: 'Cinematic post-rock soundscapes.' },
];

export const tracks: Track[] = [
  { id: 't1', title: 'Midnight Protocol', artist: 'Luna Eclipse', artistId: 'a1', album: 'Digital Horizons', albumId: 'al1', artwork: '', duration: 234, genre: 'Electronic', mood: ['Night', 'Focus'], isFavorite: false },
  { id: 't2', title: 'Neon Cascade', artist: 'Neon Drift', artistId: 'a2', album: 'Retro Future', albumId: 'al2', artwork: '', duration: 198, genre: 'Synthwave', mood: ['Energy', 'Electronic'], isFavorite: true },
  { id: 't3', title: 'Event Horizon', artist: 'Void Walker', artistId: 'a3', album: 'Singularity', albumId: 'al3', artwork: '', duration: 312, genre: 'Dark Ambient', mood: ['Night', 'Ambient'], isFavorite: false },
  { id: 't4', title: 'Crystalline', artist: 'Crystal Frequencies', artistId: 'a4', album: 'Resonance', albumId: 'al4', artwork: '', duration: 267, genre: 'Ambient', mood: ['Chill', 'Focus'], isFavorite: true },
  { id: 't5', title: 'Pulse Engine', artist: 'Phantom Circuit', artistId: 'a5', album: 'Machine Dreams', albumId: 'al5', artwork: '', duration: 186, genre: 'Techno', mood: ['Energy', 'Workout'], isFavorite: false },
  { id: 't6', title: 'Sunlit Frequencies', artist: 'Solar Flare', artistId: 'a6', album: 'Daybreak', albumId: 'al6', artwork: '', duration: 245, genre: 'Indie Electronic', mood: ['Chill', 'Focus'], isFavorite: false },
  { id: 't7', title: 'Deep Signal', artist: 'Deep Current', artistId: 'a7', album: 'Submerged', albumId: 'al7', artwork: '', duration: 298, genre: 'Deep House', mood: ['Night', 'Chill'], isFavorite: true },
  { id: 't8', title: 'Frozen Transmission', artist: 'Arctic Signal', artistId: 'a8', album: 'Northern Waves', albumId: 'al8', artwork: '', duration: 342, genre: 'Post-Rock', mood: ['Focus', 'Ambient'], isFavorite: false },
  { id: 't9', title: 'Quantum Drift', artist: 'Luna Eclipse', artistId: 'a1', album: 'Digital Horizons', albumId: 'al1', artwork: '', duration: 278, genre: 'Electronic', mood: ['Focus', 'Electronic'], isFavorite: false },
  { id: 't10', title: 'Chrome Highway', artist: 'Neon Drift', artistId: 'a2', album: 'Retro Future', albumId: 'al2', artwork: '', duration: 215, genre: 'Synthwave', mood: ['Energy', 'Electronic'], isFavorite: false },
  { id: 't11', title: 'Dark Matter', artist: 'Void Walker', artistId: 'a3', album: 'Singularity', albumId: 'al3', artwork: '', duration: 356, genre: 'Dark Ambient', mood: ['Night', 'Ambient'], isFavorite: false },
  { id: 't12', title: 'Harmonic Field', artist: 'Crystal Frequencies', artistId: 'a4', album: 'Resonance', albumId: 'al4', artwork: '', duration: 289, genre: 'Ambient', mood: ['Chill', 'Focus'], isFavorite: true },
  { id: 't13', title: 'Binary Sunset', artist: 'Phantom Circuit', artistId: 'a5', album: 'Machine Dreams', albumId: 'al5', artwork: '', duration: 203, genre: 'Techno', mood: ['Energy', 'Workout'], isFavorite: false },
  { id: 't14', title: 'Morning Signal', artist: 'Solar Flare', artistId: 'a6', album: 'Daybreak', albumId: 'al6', artwork: '', duration: 231, genre: 'Indie Electronic', mood: ['Chill'], isFavorite: false },
  { id: 't15', title: 'Tidal Wave', artist: 'Deep Current', artistId: 'a7', album: 'Submerged', albumId: 'al7', artwork: '', duration: 312, genre: 'Deep House', mood: ['Night', 'Energy'], isFavorite: false },
  { id: 't16', title: 'Aurora Borealis', artist: 'Arctic Signal', artistId: 'a8', album: 'Northern Waves', albumId: 'al8', artwork: '', duration: 378, genre: 'Post-Rock', mood: ['Focus', 'Ambient'], isFavorite: true },
];

export const albums: Album[] = [
  { id: 'al1', title: 'Digital Horizons', artist: 'Luna Eclipse', artistId: 'a1', artwork: '', year: 2024, tracks: tracks.filter(t => t.albumId === 'al1'), genre: 'Electronic' },
  { id: 'al2', title: 'Retro Future', artist: 'Neon Drift', artistId: 'a2', artwork: '', year: 2024, tracks: tracks.filter(t => t.albumId === 'al2'), genre: 'Synthwave' },
  { id: 'al3', title: 'Singularity', artist: 'Void Walker', artistId: 'a3', artwork: '', year: 2023, tracks: tracks.filter(t => t.albumId === 'al3'), genre: 'Dark Ambient' },
  { id: 'al4', title: 'Resonance', artist: 'Crystal Frequencies', artistId: 'a4', artwork: '', year: 2024, tracks: tracks.filter(t => t.albumId === 'al4'), genre: 'Ambient' },
  { id: 'al5', title: 'Machine Dreams', artist: 'Phantom Circuit', artistId: 'a5', artwork: '', year: 2023, tracks: tracks.filter(t => t.albumId === 'al5'), genre: 'Techno' },
  { id: 'al6', title: 'Daybreak', artist: 'Solar Flare', artistId: 'a6', artwork: '', year: 2024, tracks: tracks.filter(t => t.albumId === 'al6'), genre: 'Indie Electronic' },
  { id: 'al7', title: 'Submerged', artist: 'Deep Current', artistId: 'a7', artwork: '', year: 2023, tracks: tracks.filter(t => t.albumId === 'al7'), genre: 'Deep House' },
  { id: 'al8', title: 'Northern Waves', artist: 'Arctic Signal', artistId: 'a8', artwork: '', year: 2024, tracks: tracks.filter(t => t.albumId === 'al8'), genre: 'Post-Rock' },
];

export const playlists: Playlist[] = [
  { id: 'p1', title: 'Gamma Frequency', description: 'The essential GAMMA experience', artwork: '', tracks: [tracks[0], tracks[1], tracks[4], tracks[6], tracks[9]], isUserCreated: false, source: 'gamma' },
  { id: 'p2', title: 'Deep Focus', description: 'Concentration-enhancing frequencies', artwork: '', tracks: [tracks[0], tracks[3], tracks[5], tracks[7], tracks[8]], isUserCreated: false, source: 'gamma' },
  { id: 'p3', title: 'Night Signal', description: 'Late night transmissions', artwork: '', tracks: [tracks[2], tracks[6], tracks[10], tracks[14]], isUserCreated: false, source: 'gamma' },
  { id: 'p4', title: 'Energy Pulse', description: 'High-energy electronic beats', artwork: '', tracks: [tracks[1], tracks[4], tracks[9], tracks[12], tracks[14]], isUserCreated: false, source: 'gamma' },
  { id: 'p5', title: 'Ambient Space', description: 'Drift through sonic landscapes', artwork: '', tracks: [tracks[2], tracks[3], tracks[7], tracks[11], tracks[15]], isUserCreated: false, source: 'gamma' },
  { id: 'p6', title: 'Chill Orbit', description: 'Relaxed frequencies for unwinding', artwork: '', tracks: [tracks[3], tracks[5], tracks[6], tracks[11], tracks[13]], isUserCreated: false, source: 'gamma' },
];

export const moodCategories: MoodCategory[] = [
  { id: 'm1', name: 'Focus', gradient: 'from-blue-600 to-indigo-800', icon: '🎯' },
  { id: 'm2', name: 'Chill', gradient: 'from-cyan-500 to-teal-700', icon: '🌊' },
  { id: 'm3', name: 'Energy', gradient: 'from-orange-500 to-red-700', icon: '⚡' },
  { id: 'm4', name: 'Workout', gradient: 'from-rose-500 to-pink-800', icon: '💪' },
  { id: 'm5', name: 'Night', gradient: 'from-purple-700 to-indigo-900', icon: '🌙' },
  { id: 'm6', name: 'Ambient', gradient: 'from-emerald-500 to-green-800', icon: '🌿' },
  { id: 'm7', name: 'Electronic', gradient: 'from-violet-600 to-purple-900', icon: '🎛️' },
  { id: 'm8', name: 'Cinematic', gradient: 'from-amber-500 to-orange-800', icon: '🎬' },
];

export const trendingTracks = [tracks[1], tracks[4], tracks[9], tracks[12], tracks[14]];
export const recentTracks = [tracks[0], tracks[3], tracks[6], tracks[7]];
export const newReleases = [albums[0], albums[1], albums[3], albums[5], albums[7]];
