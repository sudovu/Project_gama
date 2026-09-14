import React, { useState, useEffect, useRef } from 'react';
import { 
  Play, Pause, SkipBack, SkipForward, Shuffle, Repeat, Volume2, VolumeX,
  Compass, Search, Library, Sliders, User, Download, CheckCircle, Heart,
  Maximize2, Minimize2, Radio, Activity, ExternalLink, ShieldCheck, X
} from 'lucide-react';
import { INITIAL_TRACKS, GENRES } from './mockData';
import profilePic from './assets/img_vhuwon_profile.jpg';

export default function App() {
  const [activeTab, setActiveTab] = useState('discover');
  const [tracks, setTracks] = useState(INITIAL_TRACKS);
  const [selectedGenre, setSelectedGenre] = useState('All');
  const [searchQuery, setSearchQuery] = useState('');
  
  // Playback state
  const [currentTrack, setCurrentTrack] = useState(INITIAL_TRACKS[0]);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(INITIAL_TRACKS[0].duration);
  const [volume, setVolume] = useState(80);
  const [isMuted, setIsMuted] = useState(false);
  const [isShuffle, setIsShuffle] = useState(false);
  const [repeatMode, setRepeatMode] = useState('off'); // 'off' | 'all' | 'one'
  const [favorites, setFavorites] = useState(new Set(['trk_em_01', 'trk_sk_01']));
  const [downloaded, setDownloaded] = useState(new Set(['trk_em_01']));
  const [showVideoPip, setShowVideoPip] = useState(true);
  const [isVideoFullscreen, setIsVideoFullscreen] = useState(false);

  // Equalizer state (gains in dB)
  const [eqGains, setEqGains] = useState([0, 0, 0, 0, 0]);
  const [eqPreset, setEqPreset] = useState('Flat');

  // YouTube Player Ref
  const ytPlayerRef = useRef(null);
  const visualizerCanvasRef = useRef(null);

  // Initialize YouTube Iframe Player
  useEffect(() => {
    window.onYouTubeIframeAPIReady = () => {
      ytPlayerRef.current = new window.YT.Player('yt-hidden-player', {
        height: '100%',
        width: '100%',
        videoId: currentTrack.youtubeVideoId,
        playerVars: {
          autoplay: 1,
          controls: 0,
          modestbranding: 1,
          rel: 0,
          enablejsapi: 1,
          origin: window.location.origin
        },
        events: {
          onReady: (e) => {
            e.target.setVolume(volume);
            e.target.unMute();
          },
          onStateChange: (e) => {
            if (e.data === window.YT.PlayerState.PLAYING) {
              setIsPlaying(true);
            } else if (e.data === window.YT.PlayerState.PAUSED) {
              setIsPlaying(false);
            } else if (e.data === window.YT.PlayerState.ENDED) {
              handleNext();
            }
          }
        }
      });
    };

    if (!window.YT) {
      const tag = document.createElement('script');
      tag.src = 'https://www.youtube.com/iframe_api';
      const firstScriptTag = document.getElementsByTagName('script')[0];
      firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
    } else if (window.YT && window.YT.Player) {
      window.onYouTubeIframeAPIReady();
    }

    // Windows Media Keys listener from electron preload
    if (window.electronAPI?.onMediaCommand) {
      window.electronAPI.onMediaCommand((cmd) => {
        if (cmd === 'play-pause') togglePlayPause();
        else if (cmd === 'next') handleNext();
        else if (cmd === 'previous') handlePrev();
      });
    }
  }, []);

  // Time ticker
  useEffect(() => {
    let interval = null;
    if (isPlaying) {
      interval = setInterval(() => {
        if (ytPlayerRef.current && ytPlayerRef.current.getCurrentTime) {
          const cur = ytPlayerRef.current.getCurrentTime() || 0;
          const dur = ytPlayerRef.current.getDuration() || currentTrack.duration;
          setCurrentTime(cur);
          if (dur > 0) setDuration(dur);
        } else {
          setCurrentTime((prev) => Math.min(prev + 0.5, duration));
        }
      }, 500);
    }
    return () => clearInterval(interval);
  }, [isPlaying, duration, currentTrack]);

  // Animated Visualizer Bars Canvas
  useEffect(() => {
    const canvas = visualizerCanvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    let animationId;

    const render = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      const barCount = 18;
      const barWidth = 3.5;
      const gap = 3;
      const t = Date.now() / 250;

      for (let i = 0; i < barCount; i++) {
        const height = isPlaying 
          ? Math.max(4, Math.sin(t + i * 0.45) * 14 + Math.cos(t * 1.2 + i * 0.3) * 6 + 18)
          : 3;
        const x = i * (barWidth + gap);
        const y = canvas.height - height;

        const grad = ctx.createLinearGradient(0, canvas.height, 0, 0);
        grad.addColorStop(0, '#00f0ff');
        grad.addColorStop(1, '#9d00ff');

        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.roundRect(x, y, barWidth, height, [2, 2, 0, 0]);
        ctx.fill();
      }
      animationId = requestAnimationFrame(render);
    };

    render();
    return () => cancelAnimationFrame(animationId);
  }, [isPlaying]);

  const playTrack = (track) => {
    setCurrentTrack(track);
    setCurrentTime(0);
    setDuration(track.duration);
    setIsPlaying(true);
    if (ytPlayerRef.current && ytPlayerRef.current.loadVideoById) {
      ytPlayerRef.current.loadVideoById(track.youtubeVideoId, 0);
      ytPlayerRef.current.unMute();
      ytPlayerRef.current.playVideo();
    }
  };

  const togglePlayPause = () => {
    if (isPlaying) {
      ytPlayerRef.current?.pauseVideo();
      setIsPlaying(false);
    } else {
      ytPlayerRef.current?.playVideo();
      setIsPlaying(true);
    }
  };

  const handleNext = () => {
    if (repeatMode === 'one') {
      ytPlayerRef.current?.seekTo(0, true);
      ytPlayerRef.current?.playVideo();
      return;
    }
    const idx = tracks.findIndex((t) => t.id === currentTrack.id);
    const nextIdx = isShuffle 
      ? Math.floor(Math.random() * tracks.length)
      : (idx + 1) % tracks.length;
    playTrack(tracks[nextIdx]);
  };

  const handlePrev = () => {
    if (currentTime > 3) {
      ytPlayerRef.current?.seekTo(0, true);
      setCurrentTime(0);
      return;
    }
    const idx = tracks.findIndex((t) => t.id === currentTrack.id);
    const prevIdx = (idx - 1 + tracks.length) % tracks.length;
    playTrack(tracks[prevIdx]);
  };

  const handleSeek = (e) => {
    const targetSec = parseFloat(e.target.value);
    setCurrentTime(targetSec);
    ytPlayerRef.current?.seekTo(targetSec, true);
  };

  const toggleFavorite = (trackId) => {
    const updated = new Set(favorites);
    if (updated.has(trackId)) updated.delete(trackId);
    else updated.add(trackId);
    setFavorites(updated);
  };

  const downloadSong = async (track) => {
    if (window.electronAPI?.downloadTrack) {
      await window.electronAPI.downloadTrack(track);
    }
    const updated = new Set(downloaded);
    updated.add(track.id);
    setDownloaded(updated);
  };

  const applyEqPreset = (presetName) => {
    setEqPreset(presetName);
    const presets = {
      'Flat': [0, 0, 0, 0, 0],
      'Bass Boost': [6, 4, 1, 0, -1],
      'Vocal': [-2, 1, 5, 3, 1],
      'Club': [5, 3, 0, 2, 4],
      'Treble': [-3, -1, 1, 5, 7],
      'Electronic': [5, 2, -1, 3, 6]
    };
    setEqGains(presets[presetName] || [0, 0, 0, 0, 0]);
  };

  const formatTime = (secs) => {
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const filteredTracks = tracks.filter((t) => {
    const matchesGenre = selectedGenre === 'All' || t.genre.toLowerCase().includes(selectedGenre.toLowerCase());
    const matchesQuery = t.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         t.artist.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesGenre && matchesQuery;
  });

  return (
    <div className="flex h-screen bg-gammaBg text-slate-100 aura-glow overflow-hidden">
      {/* SIDEBAR */}
      <aside className="w-64 bg-gammaSurface/90 backdrop-blur-md border-r border-slate-800/80 flex flex-col justify-between p-4 z-20">
        <div>
          {/* Brand Logo */}
          <div className="flex items-center space-x-3 px-2 py-3 mb-6">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-gammaViolet to-gammaCyan flex items-center justify-center cyan-glow">
              <Radio className="w-5 h-5 text-black" />
            </div>
            <div>
              <h1 className="text-xl font-bold tracking-wider bg-gradient-to-r from-gammaCyan to-gammaPink bg-clip-text text-transparent">
                GAMA
              </h1>
              <p className="text-[11px] text-gammaCyan tracking-widest font-mono uppercase">Windows Studio</p>
            </div>
          </div>

          {/* Navigation Items */}
          <nav className="space-y-1">
            <button
              onClick={() => setActiveTab('discover')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                activeTab === 'discover' 
                  ? 'bg-gradient-to-r from-gammaCyan/20 to-gammaViolet/20 text-gammaCyan border border-gammaCyan/30' 
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
              }`}
            >
              <Compass className="w-4 h-4" />
              <span>Discover</span>
            </button>
            <button
              onClick={() => setActiveTab('search')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                activeTab === 'search' 
                  ? 'bg-gradient-to-r from-gammaCyan/20 to-gammaViolet/20 text-gammaCyan border border-gammaCyan/30' 
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
              }`}
            >
              <Search className="w-4 h-4" />
              <span>Search</span>
            </button>
            <button
              onClick={() => setActiveTab('library')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                activeTab === 'library' 
                  ? 'bg-gradient-to-r from-gammaCyan/20 to-gammaViolet/20 text-gammaCyan border border-gammaCyan/30' 
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
              }`}
            >
              <Library className="w-4 h-4" />
              <span>My Library</span>
            </button>
            <button
              onClick={() => setActiveTab('equalizer')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                activeTab === 'equalizer' 
                  ? 'bg-gradient-to-r from-gammaCyan/20 to-gammaViolet/20 text-gammaCyan border border-gammaCyan/30' 
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
              }`}
            >
              <Sliders className="w-4 h-4" />
              <span>DSP Equalizer</span>
            </button>
          </nav>
        </div>

        {/* Developer Badge in Sidebar */}
        <div 
          onClick={() => setActiveTab('profile')}
          className="p-3 bg-gammaSurfaceElevated/80 hover:bg-gammaSurfaceElevated border border-slate-800 rounded-2xl cursor-pointer transition-all flex items-center space-x-3"
        >
          <img 
            src={profilePic} 
            alt="Developer" 
            className="w-10 h-10 rounded-xl object-cover border border-gammaCyan/40"
          />
          <div className="overflow-hidden">
            <div className="flex items-center space-x-1">
              <span className="text-xs font-bold text-slate-200 truncate">VHUWON MATHERS</span>
              <ShieldCheck className="w-3.5 h-3.5 text-gammaCyan flex-shrink-0" />
            </div>
            <p className="text-[10px] text-gammaCyan font-mono">Lead Developer</p>
          </div>
        </div>
      </aside>

      {/* MAIN CONTENT VIEW */}
      <main className="flex-1 flex flex-col overflow-hidden pb-24">
        {/* Top Header Bar */}
        <header className="h-16 border-b border-slate-800/80 px-8 flex items-center justify-between z-10">
          <div className="flex items-center space-x-4">
            <span className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-gammaCyan/10 border border-gammaCyan/30 text-gammaCyan text-xs font-mono">
              <span className="w-2 h-2 rounded-full bg-gammaCyan animate-pulse"></span>
              <span>432 Hz Master Resonator</span>
            </span>
          </div>

          <div className="relative w-80">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input 
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search tracks, artists, or YouTube link..."
              className="w-full bg-slate-900/60 border border-slate-700/60 rounded-xl pl-9 pr-4 py-1.5 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-gammaCyan"
            />
          </div>
        </header>

        {/* TAB PANELS */}
        <div className="flex-1 overflow-y-auto px-8 py-6">
          {activeTab === 'discover' && (
            <div className="space-y-8">
              {/* Genre Pills */}
              <div className="flex items-center space-x-2 overflow-x-auto pb-2">
                {GENRES.map((genre) => (
                  <button
                    key={genre}
                    onClick={() => setSelectedGenre(genre)}
                    className={`px-4 py-1.5 rounded-full text-xs font-medium transition-all ${
                      selectedGenre === genre
                        ? 'bg-gammaCyan text-black font-semibold'
                        : 'bg-slate-900/80 border border-slate-800 text-slate-400 hover:text-white'
                    }`}
                  >
                    {genre}
                  </button>
                ))}
              </div>

              {/* Hero Banner */}
              <div className="relative rounded-3xl overflow-hidden bg-gradient-to-r from-gammaViolet/30 via-slate-900 to-gammaCyan/20 border border-slate-800 p-8 flex items-center justify-between">
                <div className="max-w-md space-y-3 z-10">
                  <span className="px-2.5 py-1 rounded-md bg-gammaPink/20 text-gammaPink text-[11px] font-mono font-bold uppercase tracking-wider">
                    Featured Anthem
                  </span>
                  <h2 className="text-3xl font-extrabold tracking-tight">Eminem - Lose Yourself</h2>
                  <p className="text-xs text-slate-400">
                    Official chart-topping classic with authentic lyrics and genuine vocals, tuned to 432 Hz harmonic frequency.
                  </p>
                  <button 
                    onClick={() => playTrack(INITIAL_TRACKS[0])}
                    className="inline-flex items-center space-x-2 px-5 py-2.5 rounded-xl bg-gammaCyan text-black font-bold text-xs hover:bg-cyan-300 transition-all cyan-glow"
                  >
                    <Play className="w-4 h-4 fill-black" />
                    <span>Play Anthem Now</span>
                  </button>
                </div>
                <img 
                  src={INITIAL_TRACKS[0].artworkUrl} 
                  alt="Anthem" 
                  className="w-48 h-48 rounded-2xl object-cover shadow-2xl border border-slate-700/60 hidden md:block"
                />
              </div>

              {/* Track Grid */}
              <div>
                <h3 className="text-lg font-bold tracking-wide mb-4">Trending Signals & Hits</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {filteredTracks.map((track) => {
                    const isCurrent = currentTrack.id === track.id;
                    return (
                      <div 
                        key={track.id}
                        onClick={() => playTrack(track)}
                        className={`group p-3 rounded-2xl bg-gammaSurface/60 hover:bg-gammaSurfaceElevated border transition-all cursor-pointer flex items-center justify-between ${
                          isCurrent ? 'border-gammaCyan/60 bg-gammaSurfaceElevated' : 'border-slate-800/80 hover:border-slate-700'
                        }`}
                      >
                        <div className="flex items-center space-x-3 overflow-hidden">
                          <div className="relative w-14 h-14 rounded-xl overflow-hidden flex-shrink-0">
                            <img src={track.artworkUrl} alt={track.title} className="w-full h-full object-cover" />
                            <div className={`absolute inset-0 bg-black/40 flex items-center justify-center transition-opacity ${isCurrent ? 'opacity-100' : 'opacity-0 group-hover:opacity-100'}`}>
                              {isCurrent && isPlaying ? (
                                <Activity className="w-5 h-5 text-gammaCyan animate-bounce" />
                              ) : (
                                <Play className="w-5 h-5 text-white fill-white" />
                              )}
                            </div>
                          </div>
                          <div className="overflow-hidden">
                            <h4 className={`text-xs font-bold truncate ${isCurrent ? 'text-gammaCyan' : 'text-slate-100'}`}>
                              {track.title}
                            </h4>
                            <p className="text-[11px] text-slate-400 truncate">{track.artist}</p>
                            <span className="text-[10px] text-slate-500 font-mono">{formatTime(track.duration)} • {track.genre}</span>
                          </div>
                        </div>

                        <div className="flex items-center space-x-1" onClick={(e) => e.stopPropagation()}>
                          <button 
                            onClick={() => toggleFavorite(track.id)}
                            className={`p-2 rounded-lg transition-colors ${favorites.has(track.id) ? 'text-gammaPink' : 'text-slate-600 hover:text-slate-300'}`}
                          >
                            <Heart className={`w-4 h-4 ${favorites.has(track.id) ? 'fill-gammaPink' : ''}`} />
                          </button>
                          <button 
                            onClick={() => downloadSong(track)}
                            className={`p-2 rounded-lg transition-colors ${downloaded.has(track.id) ? 'text-gammaCyan' : 'text-slate-600 hover:text-slate-300'}`}
                          >
                            {downloaded.has(track.id) ? <CheckCircle className="w-4 h-4" /> : <Download className="w-4 h-4" />}
                          </button>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          )}

          {activeTab === 'search' && (
            <div className="space-y-6">
              <h3 className="text-xl font-bold">Search & YouTube Streaming</h3>
              <p className="text-xs text-slate-400">Search library or input any YouTube video link or 11-character video ID to stream instantly.</p>
              <div className="grid grid-cols-1 gap-2">
                {filteredTracks.map((track) => (
                  <div 
                    key={track.id}
                    onClick={() => playTrack(track)}
                    className="p-3 bg-gammaSurface/60 hover:bg-gammaSurfaceElevated rounded-xl border border-slate-800 flex items-center justify-between cursor-pointer"
                  >
                    <div className="flex items-center space-x-3">
                      <img src={track.artworkUrl} alt={track.title} className="w-10 h-10 rounded-lg object-cover" />
                      <div>
                        <p className="text-xs font-bold text-slate-200">{track.title}</p>
                        <p className="text-[11px] text-slate-400">{track.artist}</p>
                      </div>
                    </div>
                    <span className="text-xs text-slate-400 font-mono">{formatTime(track.duration)}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'library' && (
            <div className="space-y-6">
              <h3 className="text-xl font-bold">Local Offline Downloads & Favorites</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="p-5 rounded-2xl bg-gammaSurface border border-slate-800">
                  <h4 className="text-sm font-bold text-gammaCyan mb-3 flex items-center space-x-2">
                    <CheckCircle className="w-4 h-4" />
                    <span>Downloaded for Offline ({downloaded.size})</span>
                  </h4>
                  <div className="space-y-2">
                    {tracks.filter(t => downloaded.has(t.id)).map(track => (
                      <div key={track.id} onClick={() => playTrack(track)} className="flex items-center justify-between py-1.5 cursor-pointer text-xs hover:text-gammaCyan">
                        <span className="truncate">{track.title} - {track.artist}</span>
                        <span className="font-mono text-slate-500">{formatTime(track.duration)}</span>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="p-5 rounded-2xl bg-gammaSurface border border-slate-800">
                  <h4 className="text-sm font-bold text-gammaPink mb-3 flex items-center space-x-2">
                    <Heart className="w-4 h-4 fill-gammaPink" />
                    <span>Favorite Frequencies ({favorites.size})</span>
                  </h4>
                  <div className="space-y-2">
                    {tracks.filter(t => favorites.has(t.id)).map(track => (
                      <div key={track.id} onClick={() => playTrack(track)} className="flex items-center justify-between py-1.5 cursor-pointer text-xs hover:text-gammaPink">
                        <span className="truncate">{track.title} - {track.artist}</span>
                        <span className="font-mono text-slate-500">{formatTime(track.duration)}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'equalizer' && (
            <div className="max-w-2xl mx-auto space-y-8 py-4">
              <div>
                <h3 className="text-xl font-bold">Studio 5-Band Biquad DSP</h3>
                <p className="text-xs text-slate-400">Direct Form II Biquad Filter simulation across sub-bass to air frequencies.</p>
              </div>

              {/* Presets */}
              <div className="flex flex-wrap gap-2">
                {['Flat', 'Bass Boost', 'Vocal', 'Club', 'Treble', 'Electronic'].map((preset) => (
                  <button
                    key={preset}
                    onClick={() => applyEqPreset(preset)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-all ${
                      eqPreset === preset
                        ? 'bg-gradient-to-r from-gammaViolet to-gammaCyan text-black font-bold'
                        : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-white'
                    }`}
                  >
                    {preset}
                  </button>
                ))}
              </div>

              {/* Equalizer Sliders */}
              <div className="p-6 rounded-3xl bg-gammaSurface border border-slate-800 flex justify-between items-center h-64">
                {['60 Hz', '230 Hz', '910 Hz', '3.6 kHz', '14 kHz'].map((band, idx) => (
                  <div key={band} className="flex flex-col items-center h-full justify-between w-20">
                    <span className="text-[11px] font-mono text-gammaCyan">{eqGains[idx]} dB</span>
                    <input 
                      type="range" 
                      min="-12" 
                      max="12" 
                      step="1"
                      value={eqGains[idx]}
                      onChange={(e) => {
                        const val = parseInt(e.target.value);
                        const next = [...eqGains];
                        next[idx] = val;
                        setEqGains(next);
                        setEqPreset('Custom');
                      }}
                      className="h-36 -rotate-90 w-28 accent-gammaCyan cursor-pointer"
                    />
                    <span className="text-[11px] font-mono text-slate-400">{band}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'profile' && (
            <div className="max-w-xl mx-auto space-y-6 py-6">
              <div className="p-8 rounded-3xl bg-gradient-to-b from-gammaSurfaceElevated to-gammaSurface border border-slate-800 text-center space-y-4">
                <div className="relative inline-block">
                  <img 
                    src={profilePic} 
                    alt="Vhuwon Mathers" 
                    className="w-28 h-28 rounded-3xl object-cover mx-auto border-2 border-gammaCyan cyan-glow"
                  />
                  <div className="absolute -bottom-2 -right-2 p-1.5 rounded-full bg-black border border-gammaCyan">
                    <ShieldCheck className="w-5 h-5 text-gammaCyan" />
                  </div>
                </div>
                <div>
                  <h3 className="text-2xl font-black tracking-wide text-white">VHUWON MATHERS</h3>
                  <p className="text-xs text-gammaCyan font-mono tracking-wider">CREATOR & LEAD ARCHITECT</p>
                </div>
                <p className="text-xs text-slate-400 max-w-sm mx-auto leading-relaxed">
                  Permanent developer profile for the GAMA platform. Built with genuine audio playback engines, hardware acceleration, and universal compatibility.
                </p>
                <div className="pt-4 border-t border-slate-800/80 flex justify-around text-left">
                  <div>
                    <span className="text-[10px] text-slate-500 font-mono block">PLATFORM</span>
                    <span className="text-xs font-bold text-slate-200">Windows Desktop</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-500 font-mono block">VERSION</span>
                    <span className="text-xs font-bold text-slate-200">v1.0 Universal</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-500 font-mono block">STATUS</span>
                    <span className="text-xs font-bold text-emerald-400">Active</span>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>

      {/* FLOATING MINI-PLAYER (BOTTOM BAR) */}
      <footer className="fixed bottom-0 left-0 right-0 h-24 bg-gammaSurface/95 backdrop-blur-xl border-t border-slate-800 px-6 flex items-center justify-between z-30">
        {/* Track Info */}
        <div className="flex items-center space-x-4 w-1/4">
          <div className="relative w-14 h-14 rounded-xl overflow-hidden flex-shrink-0 border border-slate-700">
            <img src={currentTrack.artworkUrl} alt={currentTrack.title} className="w-full h-full object-cover" />
          </div>
          <div className="overflow-hidden">
            <h4 className="text-sm font-bold text-slate-100 truncate">{currentTrack.title}</h4>
            <p className="text-xs text-slate-400 truncate">{currentTrack.artist}</p>
            <span className="text-[10px] text-gammaCyan font-mono uppercase">{currentTrack.genre} • 432Hz</span>
          </div>
        </div>

        {/* Player Controls & Seekbar */}
        <div className="flex flex-col items-center max-w-lg w-2/4 space-y-2">
          <div className="flex items-center space-x-6">
            <button 
              onClick={() => setIsShuffle(!isShuffle)}
              className={`text-slate-400 hover:text-white transition-colors ${isShuffle ? 'text-gammaCyan' : ''}`}
            >
              <Shuffle className="w-4 h-4" />
            </button>
            <button onClick={handlePrev} className="text-slate-300 hover:text-white transition-colors">
              <SkipBack className="w-5 h-5 fill-slate-300" />
            </button>
            <button 
              onClick={togglePlayPause}
              className="w-10 h-10 rounded-full bg-gradient-to-r from-gammaCyan to-gammaViolet flex items-center justify-center text-black hover:scale-105 transition-transform cyan-glow"
            >
              {isPlaying ? <Pause className="w-5 h-5 fill-black" /> : <Play className="w-5 h-5 fill-black ml-0.5" />}
            </button>
            <button onClick={handleNext} className="text-slate-300 hover:text-white transition-colors">
              <SkipForward className="w-5 h-5 fill-slate-300" />
            </button>
            <button 
              onClick={() => setRepeatMode(repeatMode === 'off' ? 'all' : repeatMode === 'all' ? 'one' : 'off')}
              className={`text-slate-400 hover:text-white transition-colors ${repeatMode !== 'off' ? 'text-gammaPink' : ''}`}
            >
              <Repeat className="w-4 h-4" />
            </button>
          </div>

          <div className="w-full flex items-center space-x-3 text-[11px] font-mono text-slate-400">
            <span>{formatTime(currentTime)}</span>
            <input 
              type="range"
              min="0"
              max={duration || 100}
              value={currentTime}
              onChange={handleSeek}
              className="flex-1 h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-gammaCyan"
            />
            <span>{formatTime(duration)}</span>
          </div>
        </div>

        {/* Waveform Visualizer & Volume */}
        <div className="flex items-center space-x-5 w-1/4 justify-end">
          <canvas ref={visualizerCanvasRef} width={100} height={24} className="hidden md:block" />

          <div className="flex items-center space-x-2">
            <button onClick={() => setIsMuted(!isMuted)} className="text-slate-400 hover:text-white">
              {isMuted || volume === 0 ? <VolumeX className="w-4 h-4" /> : <Volume2 className="w-4 h-4" />}
            </button>
            <input 
              type="range"
              min="0"
              max="100"
              value={isMuted ? 0 : volume}
              onChange={(e) => {
                const val = parseInt(e.target.value);
                setVolume(val);
                setIsMuted(false);
                ytPlayerRef.current?.setVolume(val);
              }}
              className="w-20 h-1 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-gammaCyan"
            />
          </div>

          <button 
            onClick={() => setShowVideoPip(!showVideoPip)}
            className={`p-2 rounded-xl border transition-colors ${showVideoPip ? 'border-gammaCyan text-gammaCyan' : 'border-slate-800 text-slate-500'}`}
          >
            {showVideoPip ? <Minimize2 className="w-4 h-4" /> : <Maximize2 className="w-4 h-4" />}
          </button>
        </div>
      </footer>

      {/* EMBEDDED YOUTUBE VIDEO PIP (WITH CROSS, FULLSCREEN & MINIMIZE BUTTONS) */}
      <div 
        className={`fixed transition-all z-40 bg-black ${
          isVideoFullscreen
            ? 'inset-0 w-full h-full'
            : `bottom-28 right-6 w-80 h-48 rounded-2xl overflow-hidden border-2 border-gammaCyan/80 shadow-2xl ${
                showVideoPip ? 'opacity-100 scale-100' : 'opacity-0 pointer-events-none scale-75'
              }`
        }`}
      >
        <div id="yt-hidden-player" className="w-full h-full"></div>

        {/* Video Control Buttons Overlay */}
        <div className="absolute top-2 right-2 flex items-center space-x-1 z-50 bg-black/85 backdrop-blur-md px-1.5 py-0.5 rounded-lg border border-slate-700/80">
          <button
            onClick={() => {
              if (isVideoFullscreen) {
                setIsVideoFullscreen(false);
              } else {
                setShowVideoPip(false);
              }
            }}
            title="Minimize"
            className="p-0.5 rounded text-gammaCyan hover:bg-slate-800 transition-colors"
          >
            <Minimize2 className="w-3 h-3" />
          </button>

          <button
            onClick={() => setIsVideoFullscreen(!isVideoFullscreen)}
            title={isVideoFullscreen ? "Exit Fullscreen" : "Fullscreen"}
            className="p-0.5 rounded text-slate-200 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <Maximize2 className="w-3 h-3" />
          </button>

          <button
            onClick={() => {
              setIsVideoFullscreen(false);
              setShowVideoPip(false);
              // Switch to music-only mode; do NOT stop playback!
            }}
            title="Close video (play music only)"
            className="p-0.5 rounded text-rose-500 hover:text-rose-400 hover:bg-rose-950/40 transition-colors"
          >
            <X className="w-3 h-3" />
          </button>
        </div>
      </div>
    </div>
  );
}
