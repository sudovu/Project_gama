import React, { useState, useEffect, useRef, useMemo } from 'react';
import { 
  Play, Pause, SkipBack, SkipForward, Shuffle, Repeat, Volume2, VolumeX,
  Compass, Search, Library, Sliders, User, Download, CheckCircle, Heart,
  Maximize2, Minimize2, Radio, Activity, ExternalLink, ShieldCheck, X,
  Wifi, WifiOff, Smartphone, Laptop, AlertTriangle, AlertCircle, RefreshCw,
  Mic, Sparkles, Film, FileText, Music, Layers, BarChart2, Star, History
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
  const [isAudioOnlyMode, setIsAudioOnlyMode] = useState(false);

  // Equalizer state (gains in dB)
  const [eqGains, setEqGains] = useState([0, 0, 0, 0, 0]);
  const [eqPreset, setEqPreset] = useState('Flat');

  // Strict Color Theme Lock: User-selected theme & color harmonics (NEVER shifted by playback/artwork)
  const THEME_PRESETS = [
    { id: 'cyan', name: 'Cosmic Cyan', hex: '#00f0ff', r: 0, g: 240, b: 255 },
    { id: 'violet', name: 'Solfeggio Violet', hex: '#a855f7', r: 168, g: 85, b: 247 },
    { id: 'amber', name: 'Solar Amber', hex: '#f59e0b', r: 245, g: 158, b: 11 },
    { id: 'emerald', name: 'Emerald Zen', hex: '#10b981', r: 16, g: 185, b: 129 },
    { id: 'magenta', name: 'Electric Magenta', hex: '#ec4899', r: 236, g: 72, b: 153 }
  ];

  const [themeColor, setThemeColor] = useState(() => {
    try {
      const saved = localStorage.getItem('gama_theme_color');
      if (saved) return JSON.parse(saved);
    } catch {}
    return { id: 'cyan', name: 'Cosmic Cyan', hex: '#00f0ff', r: 0, g: 240, b: 255 };
  });

  const selectThemeColor = (preset) => {
    setThemeColor(preset);
    try {
      localStorage.setItem('gama_theme_color', JSON.stringify(preset));
    } catch {}
  };

  // Synced Lyrics state
  const [lyrics, setLyrics] = useState([]);
  const [isFetchingLyrics, setIsFetchingLyrics] = useState(false);
  const [lyricsOffset, setLyricsOffset] = useState(0);

  // Smart Library Filters
  const [smartFilter, setSmartFilter] = useState('all'); // 'all' | 'hires' | 'stream' | 'video' | 'fav' | 'download'

  // LAN Remote Control Sync state & Discovery
  const [lanConnected, setLanConnected] = useState(false);
  const [lanPhoneIp, setLanPhoneIp] = useState('192.168.1.100');
  const [lanPin, setLanPin] = useState(() => localStorage.getItem('gama_lan_pin') || '');
  const [showLanModal, setShowLanModal] = useState(false);
  const [lanStatusMsg, setLanStatusMsg] = useState('Disconnected');
  const [discoveredDevices, setDiscoveredDevices] = useState([]);
  const [isScanningLan, setIsScanningLan] = useState(false);
  const [streamToPc, setStreamToPc] = useState(false);
  const [isPushingTrack, setIsPushingTrack] = useState(false);
  const [streamInputUrl, setStreamInputUrl] = useState('');
  const [streamInputTitle, setStreamInputTitle] = useState('');
  const [playbackError, setPlaybackError] = useState(null);
  const [isReconnectingStream, setIsReconnectingStream] = useState(false);
  const [reconnectAttempt, setReconnectAttempt] = useState(0);
  const [streamMetadata, setStreamMetadata] = useState('');
  const [importedStreams, setImportedStreams] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('gama_imported_streams') || '[]');
    } catch { return []; }
  });
  const [favoriteStreams, setFavoriteStreams] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('gama_favorite_streams') || '[]');
    } catch { return []; }
  });
  const [recentStreams, setRecentStreams] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('gama_recent_streams') || '[]');
    } catch { return []; }
  });
  const lanSocketRef = useRef(null);
  const pcAudioRef = useRef(null);
  const isUsingYouTubeRef = useRef(false);
  const reconnectTimerRef = useRef(null);
  const audioCtxRef = useRef(null);
  const sourceNodeRef = useRef(null);
  const analyserRef = useRef(null);
  const biquadFiltersRef = useRef([]);

  // Deterministic Waveform Peaks Generator (SoundCloud-style)
  const waveformPeaks = useMemo(() => {
    const title = currentTrack?.title || 'gama';
    let seed = 0;
    for (let i = 0; i < title.length; i++) {
      seed = (seed * 31 + title.charCodeAt(i)) & 0xffffff;
    }
    const peaks = [];
    for (let i = 0; i < 60; i++) {
      const v = 0.2 + 0.75 * Math.abs(Math.sin((seed + i * 5) * 0.22) * Math.cos(i * 0.18));
      peaks.push(Math.max(0.18, Math.min(1.0, v)));
    }
    return peaks;
  }, [currentTrack?.id, currentTrack?.title]);

  const parseM3uText = (text, dirPath = '') => {
    const lines = text.split(/\r?\n/);
    const parsed = [];
    let curTitle = '';
    let curArtist = '';
    let curDuration = 0;

    for (let rawLine of lines) {
      const line = rawLine.trim();
      if (!line) continue;
      if (line.toUpperCase().startsWith('#EXTINF:')) {
        const info = line.substring(8).trim();
        const commaIdx = info.indexOf(',');
        if (commaIdx !== -1) {
          curDuration = parseInt(info.substring(0, commaIdx).trim(), 10) || 0;
          const name = info.substring(commaIdx + 1).trim();
          if (name.includes(' - ')) {
            const parts = name.split(' - ');
            curArtist = parts[0].trim();
            curTitle = parts.slice(1).join(' - ').trim();
          } else {
            curTitle = name;
            curArtist = 'M3U Audio';
          }
        }
      } else if (!line.startsWith('#')) {
        let streamUrl = line;
        if (!line.startsWith('http://') && !line.startsWith('https://') && !line.startsWith('rtsp://') && !line.startsWith('file://')) {
          if (dirPath) {
            const separator = dirPath.includes('/') ? '/' : '\\';
            streamUrl = `${dirPath}${separator}${line}`;
          }
        }
        const title = curTitle || (line.split(/[/\\]/).pop().replace(/\.[^/.]+$/, '') || 'Stream Track');
        const artist = curArtist || 'M3U Playlist';
        parsed.push({
          id: `m3u_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
          title,
          artist,
          streamUrl,
          duration: curDuration,
          artworkUrl: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300',
          source: 'stream'
        });
        curTitle = '';
        curArtist = '';
        curDuration = 0;
      }
    }
    return parsed;
  };

  const handlePickM3uFile = async () => {
    try {
      if (window.electronAPI?.openM3uFile) {
        const fileData = await window.electronAPI.openM3uFile();
        if (fileData?.content) {
          const parsedTracks = parseM3uText(fileData.content, fileData.dirPath);
          if (parsedTracks.length > 0) {
            const updated = [...parsedTracks, ...importedStreams];
            setImportedStreams(updated);
            try {
              localStorage.setItem('gama_imported_streams', JSON.stringify(updated.slice(0, 50)));
            } catch {}
            playTrack(parsedTracks[0]);
          }
        }
      } else {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '.m3u,.m3u8';
        input.onchange = (e) => {
          const file = e.target.files?.[0];
          if (file) {
            const reader = new FileReader();
            reader.onload = (evt) => {
              const text = evt.target.result;
              const parsed = parseM3uText(text);
              if (parsed.length > 0) {
                const updated = [...parsed, ...importedStreams];
                setImportedStreams(updated);
                try {
                  localStorage.setItem('gama_imported_streams', JSON.stringify(updated.slice(0, 50)));
                } catch {}
                playTrack(parsed[0]);
              }
            };
            reader.readAsText(file);
          }
        };
        input.click();
      }
    } catch (err) {
      console.error('[GAMA M3U Pick Error]:', err);
    }
  };

  const triggerStreamReconnect = (trackToReconnect = currentTrack) => {
    if (!trackToReconnect?.streamUrl && !streamToPc) return;
    if (reconnectAttempt >= 4) {
      setIsReconnectingStream(false);
      setPlaybackError({
        message: 'Live stream disconnected after 4 reconnect attempts.',
        url: trackToReconnect.streamUrl || `${lanPhoneIp}:8765/stream`,
        canRetry: true
      });
      return;
    }

    const nextAttempt = reconnectAttempt + 1;
    setReconnectAttempt(nextAttempt);
    setIsReconnectingStream(true);

    const backoffMs = Math.min(1000 * Math.pow(2, nextAttempt - 1), 8000);
    setPlaybackError({
      message: `Live stream connection interrupted. Auto-reconnecting (${nextAttempt}/4)...`,
      url: trackToReconnect.streamUrl || `${lanPhoneIp}:8765/stream`,
      canRetry: false
    });

    clearTimeout(reconnectTimerRef.current);
    reconnectTimerRef.current = setTimeout(() => {
      if (pcAudioRef.current) {
        if (streamToPc) {
          const trackKey = trackToReconnect ? `${trackToReconnect.id}_${Date.now()}` : `${Date.now()}`;
          pcAudioRef.current.src = `http://${lanPhoneIp}:8765/stream?token=${encodeURIComponent(lanPin)}&trackId=${trackKey}`;
        } else if (trackToReconnect.streamUrl) {
          pcAudioRef.current.src = trackToReconnect.streamUrl;
        }
        pcAudioRef.current.load();
        pcAudioRef.current.play().then(() => {
          setIsReconnectingStream(false);
          setReconnectAttempt(0);
          setPlaybackError(null);
        }).catch((err) => {
          console.warn('[GAMA Reconnect] Retry failed:', err.message);
        });
      }
    }, backoffMs);
  };

  const toggleFavoriteStream = (track) => {
    if (!track?.streamUrl) return;
    const isFav = favoriteStreams.some(s => s.streamUrl === track.streamUrl);
    let updated;
    if (isFav) {
      updated = favoriteStreams.filter(s => s.streamUrl !== track.streamUrl);
    } else {
      updated = [track, ...favoriteStreams];
    }
    setFavoriteStreams(updated);
    try {
      localStorage.setItem('gama_favorite_streams', JSON.stringify(updated.slice(0, 50)));
    } catch {}
  };

  const recordRecentStream = (track) => {
    if (!track?.streamUrl) return;
    const filtered = recentStreams.filter(s => s.streamUrl !== track.streamUrl);
    const updated = [track, ...filtered].slice(0, 25);
    setRecentStreams(updated);
    try {
      localStorage.setItem('gama_recent_streams', JSON.stringify(updated));
    } catch {}
  };

  const clearRecentStreams = () => {
    setRecentStreams([]);
    try {
      localStorage.removeItem('gama_recent_streams');
    } catch {}
  };

  const handleImportStream = (urlToImport = streamInputUrl, titleToImport = streamInputTitle) => {
    const url = urlToImport.trim();
    if (!url) return;
    const title = titleToImport.trim() || (url.split('/').pop().replace(/\.[^/.]+$/, '') || 'Live Stream');
    const artist = 'Direct Stream';
    const newTrack = {
      id: `stream_${Date.now()}`,
      title,
      artist,
      artworkUrl: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300',
      streamUrl: url,
      duration: 0,
      source: 'stream'
    };

    const updated = [newTrack, ...importedStreams.filter(s => s.streamUrl !== url)];
    setImportedStreams(updated);
    try {
      localStorage.setItem('gama_imported_streams', JSON.stringify(updated.slice(0, 30)));
    } catch {}

    recordRecentStream(newTrack);
    playTrack(newTrack);
    setStreamInputUrl('');
    setStreamInputTitle('');

    // If LAN sync is connected, also instruct phone to play stream
    if (lanConnected && lanPhoneIp) {
      sendLanCommand('play_stream', { url, title, artist });
    }
  };

  const scanLanDevices = async () => {
    setIsScanningLan(true);
    try {
      if (window.electronAPI?.discoverLanDevices) {
        const devs = await window.electronAPI.discoverLanDevices();
        setDiscoveredDevices(devs || []);
      } else {
        const probes = ['127.0.0.1', '192.168.1.100', '192.168.1.105', '192.168.0.100'];
        const found = [];
        await Promise.all(probes.map(async (ip) => {
          try {
            const ctrl = new AbortController();
            const timer = setTimeout(() => ctrl.abort(), 600);
            const res = await fetch(`http://${ip}:8765/status`, { signal: ctrl.signal });
            clearTimeout(timer);
            if (res.ok) {
              const data = await res.json();
              found.push({
                ip,
                port: 8765,
                deviceModel: data.deviceModel || 'GAMA Android',
                pairingPin: data.pairingPin || '',
                protocolVersion: data.protocolVersion || '1.2.0'
              });
            }
          } catch (_) {}
        }));
        setDiscoveredDevices(found);
      }
    } catch (_) {}
    setIsScanningLan(false);
  };

  const connectLanSync = async (ipToConnect, pinOverride) => {
    const ip = (ipToConnect || lanPhoneIp).trim();
    if (!ip) return;
    const pin = (pinOverride || lanPin).trim();

    try {
      if (lanSocketRef.current) {
        lanSocketRef.current.close();
      }

      let token = localStorage.getItem('gama_lan_token');

      // If no token or pin is specified, execute PIN pairing handshake first
      if (!token && pin) {
        setLanStatusMsg(`Pairing with ${ip}:8765 using PIN ${pin}...`);
        try {
          const pairRes = await fetch(`http://${ip}:8765/pair`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ pin, client: 'Windows Desktop' })
          });
          if (pairRes.ok) {
            const pairData = await pairRes.json();
            if (pairData.token) {
              token = pairData.token;
              localStorage.setItem('gama_lan_token', token);
              localStorage.setItem('gama_lan_pin', pin);
            }
          } else {
            setLanStatusMsg('PIN Pairing failed (HTTP 401). Check PIN displayed on phone.');
            return;
          }
        } catch (pairErr) {
          setLanStatusMsg(`Pairing request error: ${pairErr.message}`);
          return;
        }
      }

      setLanStatusMsg(`Connecting to ws://${ip}:8765/ws...`);
      const wsUrl = token ? `ws://${ip}:8765/ws?token=${encodeURIComponent(token)}` : `ws://${ip}:8765/ws`;
      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        setLanConnected(true);
        setLanStatusMsg(`Connected to GAMA Phone (${ip}) [Authenticated]`);
      };
      ws.onmessage = (e) => {
        try {
          const data = JSON.parse(e.data);
          if (data.type === 'STATUS') {
            setIsPlaying(data.isPlaying);
            if (data.positionMs !== undefined) setCurrentTime(data.positionMs / 1000);
            if (data.durationMs !== undefined && data.durationMs > 0) setDuration(data.durationMs / 1000);

            // Synchronize PC Speaker Streaming if enabled
            if (streamToPc && pcAudioRef.current) {
              const audio = pcAudioRef.current;
              const token = localStorage.getItem('gama_lan_token') || '';
              const trackKey = data.track?.id || data.track?.title || 'active';
              const streamUrl = `http://${ip}:8765/stream?token=${encodeURIComponent(token)}&trackId=${encodeURIComponent(trackKey)}`;
              if (!audio.src || !audio.src.includes(`trackId=${encodeURIComponent(trackKey)}`)) {
                audio.src = streamUrl;
                audio.load();
              }
              if (data.isPlaying && audio.paused) {
                audio.play().catch((err) => {
                  if (err.name !== 'AbortError') {
                    console.warn('[LAN Stream Play Warning]', err);
                    setPlaybackError({
                      code: 'LAN_STREAM_PLAY_ERR',
                      message: `Could not play LAN stream from phone: ${err.message}. Verify that the song is cached on the phone.`,
                      trackTitle: data.track?.title || 'Phone Stream',
                      src: streamUrl,
                      timestamp: Date.now()
                    });
                  }
                });
              } else if (!data.isPlaying && !audio.paused) {
                audio.pause();
              }
              if (data.positionMs !== undefined) {
                const targetSec = data.positionMs / 1000;
                if (Math.abs(audio.currentTime - targetSec) > 2.0) {
                  audio.currentTime = targetSec;
                }
              }
            }

            if (data.streamMetadata !== undefined) {
              setStreamMetadata(data.streamMetadata || '');
            }
            if (data.isReconnecting !== undefined) {
              setIsReconnectingStream(data.isReconnecting);
            }
            if (data.reconnectAttempt !== undefined) {
              setReconnectAttempt(data.reconnectAttempt);
            }

            if (data.track) {
              setCurrentTrack(prev => ({
                ...prev,
                id: data.track.id || prev.id,
                title: data.track.title || prev.title,
                artist: data.track.artist || prev.artist,
                artworkUrl: data.track.artworkUrl || prev.artworkUrl,
                youtubeVideoId: data.track.youtubeVideoId || prev.youtubeVideoId,
                duration: data.track.durationSeconds || prev.duration,
                audioQualityBadge: data.track.audioQualityBadge || ''
              }));
            }
          }
        } catch (_) {}
      };
      ws.onclose = (e) => {
        setLanConnected(false);
        if (pcAudioRef.current) pcAudioRef.current.pause();
        if (e.code === 1006 || e.reason?.includes('401')) {
          setLanStatusMsg('Authentication expired or invalid PIN. Please re-enter 4-digit PIN.');
          localStorage.removeItem('gama_lan_token');
        } else {
          setLanStatusMsg('Disconnected from phone');
        }
      };
      ws.onerror = () => {
        setLanConnected(false);
        if (pcAudioRef.current) pcAudioRef.current.pause();
        setLanStatusMsg('Connection failed. Verify phone IP and 4-digit PIN.');
      };
      lanSocketRef.current = ws;
    } catch (err) {
      setLanStatusMsg(`Error: ${err.message}`);
    }
  };

  const disconnectLanSync = () => {
    if (lanSocketRef.current) {
      lanSocketRef.current.close();
      lanSocketRef.current = null;
    }
    if (pcAudioRef.current) {
      pcAudioRef.current.pause();
    }
    setLanConnected(false);
    setLanStatusMsg('Disconnected');
  };

  const sendLanCommand = (action, payload = {}) => {
    if (lanSocketRef.current && lanSocketRef.current.readyState === WebSocket.OPEN) {
      lanSocketRef.current.send(JSON.stringify({ action, ...payload }));
      return true;
    }
    return false;
  };

  const pushTrackToPhone = async (track, playNow = false) => {
    const token = localStorage.getItem('gama_lan_token');
    const ip = lanPhoneIp.trim();
    if (!token || !ip) {
      setLanStatusMsg('Please connect to your phone via LAN Sync first');
      setShowLanModal(true);
      return false;
    }
    setIsPushingTrack(true);
    try {
      const res = await fetch(`http://${ip}:8765/queue/push?token=${encodeURIComponent(token)}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          id: track.id,
          title: track.title,
          artist: track.artist,
          artworkUrl: track.artworkUrl,
          streamUrl: track.streamUrl || track.audioUrl || '',
          youtubeVideoId: track.youtubeVideoId || '',
          durationSeconds: track.duration || 0,
          playNow
        })
      });
      if (res.ok) {
        setLanStatusMsg(`Queued "${track.title}" on phone!`);
        setIsPushingTrack(false);
        return true;
      } else {
        setLanStatusMsg('Queue push failed (HTTP 401/404)');
      }
    } catch (e) {
      setLanStatusMsg(`Queue push error: ${e.message}`);
    }
    setIsPushingTrack(false);
    return false;
  };

  const uploadFileToPhoneQueue = async (file, playNow = false) => {
    const token = localStorage.getItem('gama_lan_token');
    const ip = lanPhoneIp.trim();
    if (!token || !ip) {
      setLanStatusMsg('Please connect to your phone via LAN Sync first');
      setShowLanModal(true);
      return;
    }
    setLanStatusMsg(`Sending ${file.name} to phone queue...`);
    setIsPushingTrack(true);
    try {
      const arrayBuffer = await file.arrayBuffer();
      const res = await fetch(`http://${ip}:8765/queue/push?token=${encodeURIComponent(token)}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/octet-stream',
          'Authorization': `Bearer ${token}`,
          'X-Track-Filename': file.name,
          'X-Track-Title': file.name.replace(/\.[^/.]+$/, ''),
          'X-Track-Artist': 'Windows PC',
          'X-Play-Now': playNow ? 'true' : 'false'
        },
        body: arrayBuffer
      });
      if (res.ok) {
        setLanStatusMsg(`Pushed "${file.name}" straight to phone queue over Wi-Fi!`);
      } else {
        setLanStatusMsg('File upload failed (HTTP 401/404)');
      }
    } catch (err) {
      setLanStatusMsg(`Upload error: ${err.message}`);
    }
    setIsPushingTrack(false);
  };

  // YouTube Player Ref
  const ytPlayerRef = useRef(null);
  const visualizerCanvasRef = useRef(null);

  // Global Window Crash Guardrails
  useEffect(() => {
    const handleErr = (msg, url, line, col, error) => {
      console.error('[GAMA Window Error Caught]', msg, error);
      return true;
    };
    const handleRejection = (e) => {
      console.warn('[GAMA Window Unhandled Rejection Caught]', e?.reason);
      e?.preventDefault?.();
    };
    window.addEventListener('error', handleErr);
    window.addEventListener('unhandledrejection', handleRejection);
    return () => {
      window.removeEventListener('error', handleErr);
      window.removeEventListener('unhandledrejection', handleRejection);
    };
  }, []);

  // Web Audio API Setup & 5-Band Biquad Filters
  const initWebAudio = () => {
    if (!pcAudioRef.current || audioCtxRef.current) return;
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      const ctx = new AudioCtx();
      audioCtxRef.current = ctx;

      const source = ctx.createMediaElementSource(pcAudioRef.current);
      sourceNodeRef.current = source;

      // 5-band biquad filters: 60Hz, 230Hz, 910Hz, 3600Hz, 14000Hz
      const freqs = [60, 230, 910, 3600, 14000];
      const types = ['lowshelf', 'peaking', 'peaking', 'peaking', 'highshelf'];
      const filters = freqs.map((f, i) => {
        const filter = ctx.createBiquadFilter();
        filter.type = types[i];
        filter.frequency.value = f;
        filter.gain.value = eqGains[i] || 0;
        if (types[i] === 'peaking') filter.Q.value = 1.0;
        return filter;
      });
      biquadFiltersRef.current = filters;

      const analyser = ctx.createAnalyser();
      analyser.fftSize = 64;
      analyser.smoothingTimeConstant = 0.8;
      analyserRef.current = analyser;

      let node = source;
      for (const filter of filters) {
        node.connect(filter);
        node = filter;
      }
      node.connect(analyser);
      analyser.connect(ctx.destination);
    } catch (e) {
      console.warn('Web Audio DSP initialization notice:', e);
    }
  };

  useEffect(() => {
    if (biquadFiltersRef.current && biquadFiltersRef.current.length === 5) {
      eqGains.forEach((gain, i) => {
        if (biquadFiltersRef.current[i]) {
          biquadFiltersRef.current[i].gain.value = gain;
        }
      });
    }
  }, [eqGains]);

  // Strict Color Theme Lock: Automatic track/artwork-driven palette shifts on playback are permanently disabled.
  // The theme and color harmonics stay locked unless explicitly changed via Theme / Color Harmonics settings.

  // Synced Lyrics Fetching (LrcLib API)
  useEffect(() => {
    if (!currentTrack) return;
    setLyrics([]);
    setIsFetchingLyrics(true);
    const controller = new AbortController();
    const artist = encodeURIComponent(currentTrack.artist || '');
    const title = encodeURIComponent(currentTrack.title || '');
    fetch(`https://lrclib.net/api/get?artist_name=${artist}&track_name=${title}`, { signal: controller.signal })
      .then(r => r.json())
      .then(d => {
        if (d && d.syncedLyrics) {
          const lines = d.syncedLyrics.split('\n').map(l => {
            const match = l.match(/\[(\d+):(\d+\.?\d*)\](.*)/);
            if (match) {
              const sec = parseInt(match[1], 10) * 60 + parseFloat(match[2]);
              return { time: sec, text: match[3].trim() };
            }
            return null;
          }).filter(Boolean);
          setLyrics(lines);
        } else if (d && d.plainLyrics) {
          setLyrics(d.plainLyrics.split('\n').map(text => ({ time: -1, text: text.trim() })));
        } else {
          setLyrics([]);
        }
      })
      .catch(() => {})
      .finally(() => setIsFetchingLyrics(false));
    return () => controller.abort();
  }, [currentTrack?.id, currentTrack?.title]);

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
              setPlaybackError(null);
            } else if (e.data === window.YT.PlayerState.PAUSED) {
              setIsPlaying(false);
            } else if (e.data === window.YT.PlayerState.ENDED) {
              handleNext();
            }
          },
          onError: (e) => {
            let msg = 'Playback error occurred on YouTube video source.';
            if (e.data === 150 || e.data === 101) {
              msg = 'Video owner restricts embedding outside YouTube. Try playing via direct stream or connect LAN sync to phone.';
            } else if (e.data === 100) {
              msg = 'Video was not found or removed from YouTube.';
            } else if (e.data === 2) {
              msg = 'Invalid YouTube video ID parameter.';
            }
            console.error(`[YouTube Player Error ${e.data}]`, msg);
            setPlaybackError({
              code: `YT_ERR_${e.data}`,
              message: msg,
              trackTitle: currentTrack?.title || 'YouTube Track',
              timestamp: Date.now()
            });
            setIsPlaying(false);
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

  // Animated Visualizer Bars Canvas (Hybrid Web Audio FFT & Fluid Simulation)
  useEffect(() => {
    const canvas = visualizerCanvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    let animationId;
    const freqData = new Uint8Array(32);

    const render = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      const barCount = 18;
      const barWidth = 3.5;
      const gap = 3;
      const t = Date.now() / 250;

      let realFft = false;
      if (analyserRef.current && isPlaying && !isUsingYouTubeRef.current) {
        analyserRef.current.getByteFrequencyData(freqData);
        if (freqData[0] > 0 || freqData[4] > 0 || freqData[8] > 0) {
          realFft = true;
        }
      }

      for (let i = 0; i < barCount; i++) {
        let height = 3;
        if (isPlaying) {
          if (realFft) {
            height = Math.max(4, (freqData[i] / 255) * canvas.height * 0.95);
          } else {
            height = Math.max(4, Math.sin(t + i * 0.45) * 14 + Math.cos(t * 1.2 + i * 0.3) * 6 + 18);
          }
        }
        const x = i * (barWidth + gap);
        const y = canvas.height - height;

        const grad = ctx.createLinearGradient(0, canvas.height, 0, 0);
        grad.addColorStop(0, themeColor.hex || '#00f0ff');
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
  }, [isPlaying, themeColor.hex]);

  const playTrack = (track) => {
    setCurrentTrack(track);
    setCurrentTime(0);
    setPlaybackError(null);
    clearTimeout(reconnectTimerRef.current);
    setIsReconnectingStream(false);
    setReconnectAttempt(0);
    setStreamMetadata('');

    if (track.streamUrl || track.source === 'stream') {
      recordRecentStream(track);
    }

    // If LAN sync is connected, command the phone to play track
    if (lanConnected && sendLanCommand('play_track', { trackId: track.id, title: track.title, streamUrl: track.streamUrl })) {
      setIsPlaying(true);
      return;
    }

    // Determine playback mechanism:
    const audioUrl = track.streamUrl || track.localAudioUri || track.audioUrl;
    if (audioUrl) {
      isUsingYouTubeRef.current = false;
      ytPlayerRef.current?.pauseVideo();
      if (pcAudioRef.current) {
        const audio = pcAudioRef.current;
        initWebAudio();
        if (audioCtxRef.current && audioCtxRef.current.state === 'suspended') {
          audioCtxRef.current.resume().catch(() => {});
        }
        if (audio.src !== audioUrl) {
          audio.src = audioUrl;
          audio.load();
        }
        audio.volume = isMuted ? 0 : volume / 100;
        audio.play().then(() => {
          setIsPlaying(true);
          setPlaybackError(null);
        }).catch((err) => {
          if (err.name !== 'AbortError') {
            console.error('[Direct Audio Playback Failed]', err);
            setPlaybackError({
              code: err.name || 'PLAY_REJECTED',
              message: err.message || 'Audio playback failed or was blocked by the browser engine.',
              trackTitle: track.title,
              src: audioUrl,
              timestamp: Date.now()
            });
            setIsPlaying(false);
          }
        });
      }
    } else if (track.youtubeVideoId) {
      isUsingYouTubeRef.current = true;
      if (pcAudioRef.current) pcAudioRef.current.pause();
      if (ytPlayerRef.current && ytPlayerRef.current.loadVideoById) {
        setDuration(track.duration || 0);
        ytPlayerRef.current.loadVideoById(track.youtubeVideoId, 0);
        ytPlayerRef.current.unMute();
        ytPlayerRef.current.setVolume(isMuted ? 0 : volume);
        ytPlayerRef.current.playVideo();
        setIsPlaying(true);
      } else {
        setPlaybackError({
          code: 'YOUTUBE_INITIALIZING',
          message: 'YouTube engine is initializing or offline. Try playing a direct stream or connect LAN sync.',
          trackTitle: track.title,
          timestamp: Date.now()
        });
        setIsPlaying(false);
      }
    } else {
      setPlaybackError({
        code: 'NO_PLAYABLE_SOURCE',
        message: 'This track does not have a valid audio stream URL or YouTube ID.',
        trackTitle: track.title,
        timestamp: Date.now()
      });
      setIsPlaying(false);
    }
  };

  const togglePlayPause = () => {
    if (sendLanCommand('play_pause')) {
      setIsPlaying(!isPlaying);
      return;
    }
    if (isUsingYouTubeRef.current) {
      if (isPlaying) {
        clearTimeout(reconnectTimerRef.current);
        setIsReconnectingStream(false);
        ytPlayerRef.current?.pauseVideo();
        setIsPlaying(false);
      } else {
        ytPlayerRef.current?.playVideo();
        setIsPlaying(true);
      }
    } else if (pcAudioRef.current) {
      const audio = pcAudioRef.current;
      if (isPlaying) {
        clearTimeout(reconnectTimerRef.current);
        setIsReconnectingStream(false);
        audio.pause();
        setIsPlaying(false);
      } else {
        audio.play().then(() => {
          setIsPlaying(true);
        }).catch((err) => {
          if (err.name !== 'AbortError') {
            setPlaybackError({
              code: err.name || 'PLAY_REJECTED',
              message: err.message || 'Could not resume audio.',
              trackTitle: currentTrack?.title,
              timestamp: Date.now()
            });
          }
        });
      }
    }
  };

  const handleNext = () => {
    if (sendLanCommand('next')) return;
    if (repeatMode === 'one') {
      if (isUsingYouTubeRef.current) {
        ytPlayerRef.current?.seekTo(0, true);
        ytPlayerRef.current?.playVideo();
      } else if (pcAudioRef.current) {
        pcAudioRef.current.currentTime = 0;
        pcAudioRef.current.play().catch(() => {});
      }
      return;
    }
    const idx = tracks.findIndex((t) => t.id === currentTrack.id);
    const nextIdx = isShuffle 
      ? Math.floor(Math.random() * tracks.length)
      : (idx + 1) % tracks.length;
    playTrack(tracks[nextIdx]);
  };

  const handlePrev = () => {
    if (sendLanCommand('prev')) return;
    if (currentTime > 3) {
      if (isUsingYouTubeRef.current) {
        ytPlayerRef.current?.seekTo(0, true);
      } else if (pcAudioRef.current) {
        pcAudioRef.current.currentTime = 0;
      }
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
    if (sendLanCommand('seek', { positionMs: Math.round(targetSec * 1000) })) return;
    if (isUsingYouTubeRef.current) {
      ytPlayerRef.current?.seekTo(targetSec, true);
    } else if (pcAudioRef.current) {
      pcAudioRef.current.currentTime = targetSec;
    }
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

  const lyricsContainerRef = useRef(null);

  const activeLyricIdx = useMemo(() => {
    if (!lyrics || lyrics.length === 0) return -1;
    const adjustedTime = currentTime + lyricsOffset;
    for (let i = lyrics.length - 1; i >= 0; i--) {
      if (adjustedTime >= lyrics[i].time) return i;
    }
    return -1;
  }, [currentTime, lyricsOffset, lyrics]);

  useEffect(() => {
    if (activeTab === 'lyrics' && activeLyricIdx >= 0) {
      const lineEl = document.getElementById(`lyric-line-${activeLyricIdx}`);
      if (lineEl) {
        lineEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }
  }, [activeLyricIdx, activeTab]);

  const filteredTracks = tracks.filter((t) => {
    const matchesGenre = selectedGenre === 'All' || t.genre.toLowerCase().includes(selectedGenre.toLowerCase());
    const matchesQuery = t.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         t.artist.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesGenre && matchesQuery;
  });

  const smartFilteredTracks = useMemo(() => {
    return tracks.filter(t => {
      if (smartFilter === 'hires') {
        return t.codec === 'FLAC' || t.audioQualityBadge?.toLowerCase().includes('hi-res') || t.audioQualityBadge?.toLowerCase().includes('lossless') || (t.bitrateKbps && t.bitrateKbps >= 900);
      }
      if (smartFilter === 'stream') {
        return t.source === 'stream' || t.source === 'live' || t.streamUrl || t.duration <= 0;
      }
      if (smartFilter === 'video') {
        return Boolean(t.youtubeVideoId) || t.source === 'youtube';
      }
      if (smartFilter === 'fav') {
        return favorites.has(t.id);
      }
      if (smartFilter === 'download') {
        return downloaded.has(t.id);
      }
      return true;
    });
  }, [tracks, smartFilter, favorites, downloaded]);

  return (
    <div 
      className="flex h-screen bg-gammaBg text-slate-100 aura-glow overflow-hidden relative transition-colors duration-1000"
      style={{
        backgroundImage: `radial-gradient(circle at 10% 20%, rgba(${themeColor.r}, ${themeColor.g}, ${themeColor.b}, 0.15) 0%, transparent 45%), radial-gradient(circle at 90% 80%, rgba(${themeColor.r}, ${themeColor.g}, ${themeColor.b}, 0.10) 0%, transparent 40%)`
      }}
    >
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
              onClick={() => setActiveTab('lyrics')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                activeTab === 'lyrics' 
                  ? 'bg-gradient-to-r from-gammaCyan/20 to-gammaViolet/20 text-gammaCyan border border-gammaCyan/30' 
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
              }`}
            >
              <Mic className="w-4 h-4" />
              <span>Karaoke Lyrics</span>
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

            <button
              onClick={() => {
                setShowLanModal(true);
                scanLanDevices();
              }}
              className={`inline-flex items-center space-x-2 px-3 py-1 rounded-full text-xs font-mono transition-all border ${
                lanConnected 
                  ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/50 hover:bg-emerald-500/30' 
                  : 'bg-slate-800/80 text-slate-300 border-slate-700 hover:bg-slate-700'
              }`}
            >
              {lanConnected ? (
                <>
                  <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping"></span>
                  <span>LAN Synced: {lanPhoneIp}</span>
                </>
              ) : (
                <>
                  <Wifi className="w-3.5 h-3.5 text-slate-400" />
                  <span>LAN Sync (Off)</span>
                </>
              )}
            </button>

            {/* Strict Theme / Color Harmonics Selector */}
            <div className="hidden lg:flex items-center space-x-1.5 px-2.5 py-1 rounded-full bg-slate-900/80 border border-slate-800">
              <span className="text-[10px] font-mono text-slate-500 mr-1">THEME:</span>
              {THEME_PRESETS.map((preset) => (
                <button
                  key={preset.id}
                  onClick={() => selectThemeColor(preset)}
                  title={`Select ${preset.name} (Strict Theme Lock)`}
                  className={`w-3.5 h-3.5 rounded-full transition-all ${
                    themeColor.id === preset.id ? 'scale-125 ring-2 ring-white/80' : 'opacity-50 hover:opacity-100 hover:scale-110'
                  }`}
                  style={{ backgroundColor: preset.hex }}
                />
              ))}
            </div>
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
                          {lanConnected && (
                            <button
                              title="Push to Phone Queue"
                              onClick={() => pushTrackToPhone(track)}
                              className="p-2 rounded-lg text-slate-500 hover:text-gammaCyan transition-colors"
                            >
                              <Smartphone className="w-4 h-4" />
                            </button>
                          )}
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
              {/* Direct Stream URL & M3U Playlist Import Panel */}
              <div className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-3">
                <div className="flex items-center justify-between">
                  <h4 className="text-sm font-bold text-gammaCyan flex items-center space-x-2">
                    <Radio className="w-4 h-4 text-gammaCyan" />
                    <span>Direct Stream URL &amp; M3U Playlist Import</span>
                  </h4>
                  <span className="text-[10px] font-mono text-slate-500">HLS • MP3 • FLAC • AAC • M3U8</span>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-2">
                  <input
                    type="text"
                    value={streamInputTitle}
                    onChange={(e) => setStreamInputTitle(e.target.value)}
                    placeholder="Stream Title (Optional)"
                    className="p-2 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:border-gammaCyan"
                  />
                  <input
                    type="text"
                    value={streamInputUrl}
                    onChange={(e) => setStreamInputUrl(e.target.value)}
                    placeholder="Paste Stream URL (http://...mp3, .m3u8, etc.)"
                    className="md:col-span-2 p-2 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:border-gammaCyan"
                  />
                </div>
                <div className="flex items-center justify-between flex-wrap gap-2 pt-1">
                  <div className="flex items-center gap-1.5 flex-wrap">
                    <span className="text-[10px] text-slate-500 font-mono">Quick Streams:</span>
                    <button
                      onClick={() => handleImportStream('https://somafm.com/groovesalad130.pls', 'SomaFM Groove Salad')}
                      className="text-[10px] px-2 py-0.5 rounded bg-slate-800/80 hover:bg-slate-700 text-slate-300 font-mono"
                    >
                      Groove Salad
                    </button>
                    <button
                      onClick={() => handleImportStream('https://somafm.com/defcon.pls', 'SomaFM DEF CON')}
                      className="text-[10px] px-2 py-0.5 rounded bg-slate-800/80 hover:bg-slate-700 text-slate-300 font-mono"
                    >
                      DEF CON Radio
                    </button>
                    <button
                      onClick={() => handleImportStream('https://somafm.com/dronezone130.pls', 'Drone Zone')}
                      className="text-[10px] px-2 py-0.5 rounded bg-slate-800/80 hover:bg-slate-700 text-slate-300 font-mono"
                    >
                      Drone Zone
                    </button>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={handlePickM3uFile}
                      className="text-xs px-3 py-1.5 rounded-lg font-bold bg-slate-800 hover:bg-slate-700 text-gammaCyan border border-gammaCyan/40 transition-colors flex items-center gap-1.5 font-mono"
                    >
                      📁 Pick .m3u / .m3u8 File
                    </button>
                    <button
                      onClick={() => handleImportStream()}
                      disabled={!streamInputUrl.trim()}
                      className="text-xs px-3 py-1.5 rounded-lg font-bold bg-gammaCyan text-black hover:bg-cyan-300 transition-colors disabled:opacity-40"
                    >
                      ▶ Stream Now
                    </button>
                  </div>
                </div>

                {/* Favorite Streams Section */}
                {favoriteStreams.length > 0 && (
                  <div className="pt-2 border-t border-slate-800/80 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-[11px] font-bold text-slate-300 font-mono flex items-center gap-1.5">
                        <Star className="w-3.5 h-3.5 text-gammaCyan fill-gammaCyan" />
                        <span>FAVORITE STREAMS ({favoriteStreams.length})</span>
                      </span>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                      {favoriteStreams.map((st) => (
                        <div
                          key={st.streamUrl}
                          className="p-2 rounded-xl bg-slate-950/70 border border-slate-800 flex items-center justify-between"
                        >
                          <div className="flex items-center space-x-2 min-w-0 pr-2">
                            <button
                              onClick={() => toggleFavoriteStream(st)}
                              title="Remove from favorites"
                              className="text-gammaCyan hover:text-slate-400 p-1"
                            >
                              <Star className="w-3.5 h-3.5 fill-gammaCyan" />
                            </button>
                            <div className="truncate">
                              <p className="text-xs font-bold text-slate-200 truncate">{st.title}</p>
                              <p className="text-[10px] text-slate-500 font-mono truncate">{st.streamUrl}</p>
                            </div>
                          </div>
                          <button
                            onClick={() => playTrack(st)}
                            className="text-[11px] px-2.5 py-1 rounded bg-gammaCyan/20 hover:bg-gammaCyan text-gammaCyan hover:text-black font-bold transition-colors font-mono shrink-0"
                          >
                            ▶ Play
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Recent Stream History Section */}
                {recentStreams.length > 0 && (
                  <div className="pt-2 border-t border-slate-800/80 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-[11px] font-bold text-slate-300 font-mono flex items-center gap-1.5">
                        <History className="w-3.5 h-3.5 text-slate-400" />
                        <span>RECENT STREAMS ({recentStreams.length})</span>
                      </span>
                      <button
                        onClick={clearRecentStreams}
                        className="text-[10px] text-slate-500 hover:text-rose-400 font-mono transition-colors"
                      >
                        Clear History
                      </button>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                      {recentStreams.map((st) => {
                        const isFav = favoriteStreams.some(s => s.streamUrl === st.streamUrl);
                        return (
                          <div
                            key={st.streamUrl}
                            className="p-2 rounded-xl bg-slate-950/70 border border-slate-800 flex items-center justify-between"
                          >
                            <div className="flex items-center space-x-2 min-w-0 pr-2">
                              <button
                                onClick={() => toggleFavoriteStream(st)}
                                title={isFav ? "Remove from favorites" : "Add to favorites"}
                                className={`p-1 transition-colors ${isFav ? 'text-gammaCyan' : 'text-slate-600 hover:text-gammaCyan'}`}
                              >
                                <Star className={`w-3.5 h-3.5 ${isFav ? 'fill-gammaCyan' : ''}`} />
                              </button>
                              <div className="truncate">
                                <p className="text-xs font-bold text-slate-200 truncate">{st.title}</p>
                                <p className="text-[10px] text-slate-500 font-mono truncate">{st.streamUrl}</p>
                              </div>
                            </div>
                            <button
                              onClick={() => playTrack(st)}
                              className="text-[11px] px-2.5 py-1 rounded bg-gammaCyan/20 hover:bg-gammaCyan text-gammaCyan hover:text-black font-bold transition-colors font-mono shrink-0"
                            >
                              ▶ Play
                            </button>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}
              </div>

              <h3 className="text-xl font-bold">Search &amp; YouTube Streaming</h3>
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
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                  <h3 className="text-xl font-bold">Media Library &amp; Frequencies</h3>
                  <p className="text-xs text-slate-400">Universal audiophile collection with high-res lossless verification.</p>
                </div>

                {/* Smart Filter Chips */}
                <div className="flex items-center gap-1.5 flex-wrap">
                  {[
                    { id: 'all', label: 'All', count: tracks.length },
                    { id: 'hires', label: 'Hi-Res Lossless', count: tracks.filter(t => t.codec === 'FLAC' || t.audioQualityBadge?.includes('Hi-Res') || t.audioQualityBadge?.includes('Lossless') || (t.bitrateKbps && t.bitrateKbps >= 900)).length },
                    { id: 'stream', label: 'Live Streams', count: tracks.filter(t => t.source === 'stream' || t.streamUrl).length },
                    { id: 'video', label: 'Videos', count: tracks.filter(t => t.youtubeVideoId).length },
                    { id: 'fav', label: 'Favorites', count: favorites.size },
                    { id: 'download', label: 'Downloaded', count: downloaded.size }
                  ].map(chip => (
                    <button
                      key={chip.id}
                      onClick={() => setSmartFilter(chip.id)}
                      className={`px-3 py-1.5 rounded-full text-xs font-mono transition-all flex items-center space-x-1.5 border ${
                        smartFilter === chip.id
                          ? 'bg-gradient-to-r from-gammaCyan to-gammaViolet text-black font-bold border-transparent'
                          : 'bg-slate-900/80 text-slate-400 border-slate-800 hover:text-white hover:border-slate-700'
                      }`}
                    >
                      <span>{chip.label}</span>
                      <span className={`text-[10px] px-1.5 py-0.2 rounded-full ${smartFilter === chip.id ? 'bg-black/20 text-black' : 'bg-slate-800 text-slate-300'}`}>
                        {chip.count}
                      </span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Filtered Track Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                {smartFilteredTracks.map(track => {
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
                        <div className="relative w-12 h-12 rounded-xl overflow-hidden flex-shrink-0">
                          <img src={track.artworkUrl} alt={track.title} className="w-full h-full object-cover" />
                          <div className={`absolute inset-0 bg-black/40 flex items-center justify-center transition-opacity ${isCurrent ? 'opacity-100' : 'opacity-0 group-hover:opacity-100'}`}>
                            {isCurrent && isPlaying ? (
                              <Activity className="w-4 h-4 text-gammaCyan animate-bounce" />
                            ) : (
                              <Play className="w-4 h-4 text-white fill-white" />
                            )}
                          </div>
                        </div>
                        <div className="overflow-hidden">
                          <h4 className={`text-xs font-bold truncate ${isCurrent ? 'text-gammaCyan' : 'text-slate-100'}`}>
                            {track.title}
                          </h4>
                          <p className="text-[11px] text-slate-400 truncate">{track.artist}</p>
                          <div className="flex items-center space-x-1.5 mt-0.5">
                            <span className="text-[10px] text-slate-500 font-mono">{formatTime(track.duration)}</span>
                            {track.codec === 'FLAC' && (
                              <span className="text-[9px] px-1 py-0.2 rounded bg-gammaCyan/10 text-gammaCyan border border-gammaCyan/30 font-mono font-bold">
                                FLAC 24-bit
                              </span>
                            )}
                            {track.source === 'stream' && (
                              <span className="text-[9px] px-1 py-0.2 rounded bg-rose-500/10 text-rose-400 border border-rose-500/30 font-mono font-bold">
                                STREAM
                              </span>
                            )}
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center space-x-1" onClick={(e) => e.stopPropagation()}>
                        <button 
                          onClick={() => toggleFavorite(track.id)}
                          className={`p-1.5 rounded-lg transition-colors ${favorites.has(track.id) ? 'text-gammaPink' : 'text-slate-600 hover:text-slate-300'}`}
                        >
                          <Heart className={`w-3.5 h-3.5 ${favorites.has(track.id) ? 'fill-gammaPink' : ''}`} />
                        </button>
                        <button 
                          onClick={() => downloadSong(track)}
                          className={`p-1.5 rounded-lg transition-colors ${downloaded.has(track.id) ? 'text-gammaCyan' : 'text-slate-600 hover:text-slate-300'}`}
                        >
                          {downloaded.has(track.id) ? <CheckCircle className="w-3.5 h-3.5" /> : <Download className="w-3.5 h-3.5" />}
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Offline & Favorites Summary Panels */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4">
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

          {activeTab === 'lyrics' && (
            <div className="max-w-3xl mx-auto space-y-6 py-4 flex flex-col h-full">
              {/* Lyrics Header */}
              <div className="flex items-center justify-between border-b border-slate-800/80 pb-4">
                <div className="flex items-center space-x-4">
                  <img src={currentTrack.artworkUrl} alt={currentTrack.title} className="w-14 h-14 rounded-2xl object-cover shadow-lg border border-slate-700/80" />
                  <div>
                    <h3 className="text-lg font-bold text-slate-100 truncate">{currentTrack.title}</h3>
                    <p className="text-xs text-slate-400 truncate">{currentTrack.artist}</p>
                    <div className="flex items-center space-x-2 mt-1">
                      <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-gammaCyan/10 text-gammaCyan border border-gammaCyan/30">
                        Karaoke Mode • LrcLib Synced
                      </span>
                      {currentTrack.audioQualityBadge && (
                        <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-slate-800 text-slate-300">
                          {currentTrack.audioQualityBadge}
                        </span>
                      )}
                    </div>
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  <span className="text-xs text-slate-500 font-mono">Sync Offset:</span>
                  <button 
                    onClick={() => setLyricsOffset(o => o - 0.5)}
                    className="px-2 py-1 text-xs font-mono bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg"
                    title="Advance -0.5s"
                  >
                    -0.5s
                  </button>
                  <button 
                    onClick={() => setLyricsOffset(0)}
                    className="px-2 py-1 text-xs font-mono bg-slate-800 hover:bg-slate-700 text-gammaCyan rounded-lg"
                    title="Reset Offset"
                  >
                    {lyricsOffset >= 0 ? `+${lyricsOffset.toFixed(1)}s` : `${lyricsOffset.toFixed(1)}s`}
                  </button>
                  <button 
                    onClick={() => setLyricsOffset(o => o + 0.5)}
                    className="px-2 py-1 text-xs font-mono bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg"
                    title="Delay +0.5s"
                  >
                    +0.5s
                  </button>
                </div>
              </div>

              {/* Lyrics Scrolling Body */}
              <div className="flex-1 overflow-y-auto space-y-4 py-8 px-4 text-center select-none" ref={lyricsContainerRef}>
                {isFetchingLyrics ? (
                  <div className="py-20 flex flex-col items-center justify-center space-y-3">
                    <Activity className="w-8 h-8 text-gammaCyan animate-bounce" />
                    <p className="text-xs font-mono text-slate-400">Fetching synchronized .lrc lyrics...</p>
                  </div>
                ) : lyrics && lyrics.length > 0 ? (
                  lyrics.map((line, idx) => {
                    const isActive = idx === activeLyricIdx;
                    return (
                      <p
                        key={idx}
                        id={`lyric-line-${idx}`}
                        onClick={() => {
                          const targetTime = Math.max(0, line.time - lyricsOffset);
                          if (isUsingYouTubeRef.current && ytPlayerRef.current?.seekTo) {
                            ytPlayerRef.current.seekTo(targetTime, true);
                          } else if (pcAudioRef.current) {
                            pcAudioRef.current.currentTime = targetTime;
                          }
                          setCurrentTime(targetTime);
                        }}
                        className={`cursor-pointer transition-all duration-300 rounded-xl py-2 px-4 ${
                          isActive 
                            ? 'text-2xl md:text-3xl font-black scale-105' 
                            : 'text-base md:text-lg text-slate-500 hover:text-slate-300 font-semibold opacity-70 hover:opacity-100'
                        }`}
                        style={{
                          color: isActive ? themeColor.hex : undefined,
                          textShadow: isActive ? `0 0 20px ${themeColor.hex}88` : undefined
                        }}
                      >
                        {line.text}
                      </p>
                    );
                  })
                ) : (
                  <div className="py-20 flex flex-col items-center justify-center space-y-3">
                    <Mic className="w-10 h-10 text-slate-600" />
                    <p className="text-sm text-slate-400 font-medium">No synchronized lyrics found for this track.</p>
                    <p className="text-xs text-slate-600 font-mono">Playing with 432 Hz Master Resonator &amp; 5-Band Hardware DSP.</p>
                  </div>
                )}
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

              {/* Color Harmonics & Theme Lock */}
              <div className="p-4 rounded-2xl bg-slate-900/60 border border-slate-800 space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-slate-200">Color Harmonics (Strict Theme Lock)</span>
                  <span className="text-[10px] font-mono text-gammaCyan">{themeColor.name}</span>
                </div>
                <div className="flex flex-wrap gap-2 pt-1">
                  {THEME_PRESETS.map((preset) => (
                    <button
                      key={preset.id}
                      onClick={() => selectThemeColor(preset)}
                      className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-all flex items-center space-x-2 border ${
                        themeColor.id === preset.id
                          ? 'border-white text-white font-bold bg-slate-800'
                          : 'border-slate-800 text-slate-400 hover:text-white bg-slate-950'
                      }`}
                    >
                      <span className="w-3 h-3 rounded-full" style={{ backgroundColor: preset.hex }} />
                      <span>{preset.name}</span>
                    </button>
                  ))}
                </div>
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

      {/* PLAYBACK ERROR BANNER */}
      {playbackError && (
        <div className="fixed bottom-28 left-1/2 -translate-x-1/2 z-50 max-w-xl w-full px-4 animate-in fade-in slide-in-from-bottom-2 duration-300">
          <div className="bg-rose-950/95 border border-rose-500/70 backdrop-blur-xl rounded-2xl p-4 shadow-2xl flex items-start space-x-3 text-slate-100">
            <AlertCircle className="w-5 h-5 text-rose-400 flex-shrink-0 mt-0.5" />
            <div className="flex-1 overflow-hidden">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-rose-300 font-mono tracking-wider uppercase">Playback Error</span>
                <button
                  onClick={() => setPlaybackError(null)}
                  className="text-slate-400 hover:text-white text-xs font-mono ml-2"
                >
                  ✕ Dismiss
                </button>
              </div>
              <p className="text-xs text-slate-200 mt-1 leading-relaxed">{playbackError.message}</p>
              {playbackError.url && (
                <p className="text-[10px] font-mono text-rose-300/80 mt-1 truncate">Source: {playbackError.url}</p>
              )}
            </div>
            {playbackError.canRetry && (
              <button
                onClick={() => {
                  setPlaybackError(null);
                  playTrack(currentTrack);
                }}
                className="px-3 py-1.5 bg-rose-600 hover:bg-rose-500 text-white rounded-lg text-xs font-bold font-mono flex items-center space-x-1 flex-shrink-0 self-center transition-colors"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span>Retry</span>
              </button>
            )}
          </div>
        </div>
      )}

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
            <div className="flex items-center space-x-1.5 mt-0.5">
              {(currentTrack.source === 'stream' || currentTrack.streamUrl) && (
                <span className="text-[9px] px-1.5 py-0.2 rounded bg-rose-500/20 text-rose-400 font-mono font-bold uppercase border border-rose-500/40 animate-pulse">
                  ● LIVE
                </span>
              )}
              {streamMetadata ? (
                <span className="text-[10px] text-gammaCyan font-mono truncate max-w-[140px]">{streamMetadata}</span>
              ) : (
                <span className="text-[10px] text-gammaCyan font-mono uppercase">{currentTrack.genre || 'Frequency'} • 432Hz</span>
              )}
              {currentTrack.audioQualityBadge && (
                <span className="text-[9px] px-1.5 py-0.2 rounded bg-gammaCyan/20 text-gammaCyan font-mono uppercase border border-gammaCyan/40">
                  {currentTrack.audioQualityBadge}
                </span>
              )}
            </div>
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
            {(currentTrack.source === 'stream' || currentTrack.streamUrl) && (!duration || duration <= 0) ? (
              <div className="flex-1 flex items-center space-x-2">
                <div className="flex-1 h-3 bg-slate-900/80 rounded-lg overflow-hidden relative border border-rose-500/30">
                  <div className="absolute inset-0 bg-gradient-to-r from-gammaCyan via-rose-500 to-gammaViolet animate-pulse opacity-80"></div>
                  <div className="absolute inset-0 flex items-center justify-center text-[9px] font-bold text-white tracking-widest font-mono">
                    LIVE STREAMING • BUFFER RESILIENT
                  </div>
                </div>
                <button
                  onClick={() => toggleFavoriteStream(currentTrack)}
                  title={favoriteStreams.some(s => s.streamUrl === currentTrack.streamUrl) ? "Unfavorite stream" : "Favorite stream"}
                  className="p-1 text-slate-400 hover:text-gammaCyan transition-colors shrink-0"
                >
                  <Star className={`w-3.5 h-3.5 ${favoriteStreams.some(s => s.streamUrl === currentTrack.streamUrl) ? 'text-gammaCyan fill-gammaCyan' : ''}`} />
                </button>
              </div>
            ) : (
              <div className="flex-1 flex flex-col justify-center space-y-1 group py-1">
                {/* 60-Bar Waveform Scrubber */}
                <div 
                  className="h-4 flex items-end space-x-0.5 cursor-pointer"
                  onClick={(e) => {
                    const rect = e.currentTarget.getBoundingClientRect();
                    const clickX = e.clientX - rect.left;
                    const pct = Math.max(0, Math.min(1, clickX / rect.width));
                    const newTime = pct * (duration || 0);
                    handleSeek({ target: { value: newTime } });
                  }}
                >
                  {waveformPeaks.map((peak, idx) => {
                    const barProgress = (idx + 1) / waveformPeaks.length;
                    const currentProgress = duration > 0 ? currentTime / duration : 0;
                    const isPlayed = barProgress <= currentProgress;
                    return (
                      <div
                        key={idx}
                        className="flex-1 rounded-full transition-all duration-75"
                        style={{
                          height: `${Math.round(peak * 100)}%`,
                          backgroundColor: isPlayed ? themeColor.hex : '#334155',
                          opacity: isPlayed ? 1 : 0.45,
                          boxShadow: isPlayed ? `0 0 4px ${themeColor.hex}88` : 'none'
                        }}
                      />
                    );
                  })}
                </div>
                {/* Precise slider */}
                <input 
                  type="range"
                  min="0"
                  max={duration || 100}
                  value={currentTime}
                  onChange={handleSeek}
                  className="w-full h-1 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-gammaCyan"
                />
              </div>
            )}
            <span>{(currentTrack.source === 'stream' || currentTrack.streamUrl) && (!duration || duration <= 0) ? 'LIVE' : formatTime(duration)}</span>
          </div>
        </div>

        {/* Waveform Visualizer, Audio/Video Switcher & Volume */}
        <div className="flex items-center space-x-3 w-1/4 justify-end">
          <canvas ref={visualizerCanvasRef} width={90} height={24} className="hidden md:block rounded" />

          <button 
            onClick={() => setIsAudioOnlyMode(!isAudioOnlyMode)}
            title={isAudioOnlyMode ? "Audio-Only Active (Video hidden)" : "Video Mode Active"}
            className={`p-2 rounded-xl border transition-colors ${
              isAudioOnlyMode 
                ? 'border-amber-400 text-amber-400 bg-amber-400/10' 
                : 'border-slate-800 text-slate-400 hover:text-white'
            }`}
          >
            {isAudioOnlyMode ? <Music className="w-4 h-4" /> : <Film className="w-4 h-4" />}
          </button>

          <div className="flex items-center space-x-2">
            <button onClick={() => {
              const nextMute = !isMuted;
              setIsMuted(nextMute);
              if (nextMute) {
                ytPlayerRef.current?.mute();
                if (pcAudioRef.current) pcAudioRef.current.muted = true;
              } else {
                ytPlayerRef.current?.unMute();
                if (pcAudioRef.current) pcAudioRef.current.muted = false;
              }
            }} className="text-slate-400 hover:text-white">
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
                if (pcAudioRef.current) {
                  pcAudioRef.current.volume = val / 100;
                  pcAudioRef.current.muted = false;
                }
              }}
              className="w-16 h-1 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-gammaCyan"
            />
          </div>

          <button 
            onClick={() => setShowVideoPip(!showVideoPip)}
            className={`p-2 rounded-xl border transition-colors ${showVideoPip && !isAudioOnlyMode ? 'border-gammaCyan text-gammaCyan' : 'border-slate-800 text-slate-500'}`}
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
                showVideoPip && !isAudioOnlyMode ? 'opacity-100 scale-100' : 'opacity-0 pointer-events-none scale-75'
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

      {/* LAN REMOTE CONTROL SYNC MODAL */}
      {showLanModal && (
        <div className="fixed inset-0 bg-black/75 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-gammaSurfaceElevated border border-gammaCyan/40 rounded-2xl w-full max-w-md p-6 shadow-2xl relative">
            <button 
              onClick={() => setShowLanModal(false)}
              className="absolute right-4 top-4 text-slate-400 hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center space-x-3 mb-4">
              <div className="w-10 h-10 rounded-xl bg-gammaCyan/10 border border-gammaCyan/30 flex items-center justify-center text-gammaCyan">
                <Smartphone className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-slate-100">Cross-Device LAN Sync</h3>
                <p className="text-xs text-slate-400">Control phone playback & stream live telemetry over Wi-Fi</p>
              </div>
            </div>

            <div className="space-y-4 mb-6">
              {/* Auto Discovery / Quick Connect */}
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-mono text-slate-300">mDNS ZERO-CONFIG DISCOVERY</span>
                <button
                  onClick={scanLanDevices}
                  disabled={isScanningLan}
                  className="text-[11px] text-gammaCyan hover:underline font-mono"
                >
                  {isScanningLan ? 'Scanning Wi-Fi...' : '🔄 Scan for Phones'}
                </button>
              </div>

              {discoveredDevices.length > 0 && (
                <div className="space-y-2 p-2.5 rounded-xl bg-slate-900 border border-gammaCyan/30">
                  <div className="text-[10px] text-gammaCyan font-mono uppercase tracking-wider">Discovered GAMA Devices</div>
                  {discoveredDevices.map((dev) => (
                    <div key={dev.ip} className="flex items-center justify-between p-2 rounded-lg bg-slate-800/80 hover:bg-slate-800">
                      <div>
                        <div className="text-xs font-bold text-slate-200">{dev.deviceModel}</div>
                        <div className="text-[10px] text-slate-400 font-mono">{dev.ip}:8765 {dev.pairingPin ? `• PIN: ${dev.pairingPin}` : ''}</div>
                      </div>
                      <button
                        onClick={() => {
                          setLanPhoneIp(dev.ip);
                          if (dev.pairingPin) setLanPin(dev.pairingPin);
                          connectLanSync(dev.ip, dev.pairingPin);
                        }}
                        className="py-1 px-2.5 bg-gammaCyan text-black font-bold text-xs rounded-md hover:bg-gammaCyan/80"
                      >
                        1-Click Connect
                      </button>
                    </div>
                  ))}
                </div>
              )}

              <div className="grid grid-cols-3 gap-2">
                <div className="col-span-2">
                  <label className="text-xs font-mono text-slate-300 block mb-1">PHONE IP ADDRESS</label>
                  <input 
                    type="text" 
                    value={lanPhoneIp}
                    onChange={(e) => setLanPhoneIp(e.target.value)}
                    placeholder="e.g. 192.168.1.150"
                    className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-sm text-slate-200 font-mono focus:border-gammaCyan focus:outline-none"
                  />
                </div>
                <div>
                  <label className="text-xs font-mono text-slate-300 block mb-1">4-DIGIT PIN</label>
                  <input 
                    type="text" 
                    maxLength={4}
                    value={lanPin}
                    onChange={(e) => setLanPin(e.target.value.replace(/\D/g, ''))}
                    placeholder="PIN"
                    className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-sm text-slate-200 font-mono text-center tracking-widest focus:border-gammaCyan focus:outline-none"
                  />
                </div>
              </div>

              <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800 text-xs text-slate-300 space-y-1">
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">Connection Status:</span>
                  <span className={`font-mono font-bold ${lanConnected ? 'text-emerald-400' : 'text-amber-400'}`}>
                    {lanConnected ? 'ACTIVE (AUTHENTICATED)' : 'DISCONNECTED'}
                  </span>
                </div>
                <p className="text-[11px] text-slate-400 font-mono">{lanStatusMsg}</p>
              </div>

              <div className="flex space-x-2">
                <button
                  onClick={() => connectLanSync('127.0.0.1')}
                  className="flex-1 py-1.5 px-2 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs rounded-lg font-mono"
                >
                  Localhost
                </button>
                <button
                  onClick={() => connectLanSync(lanPhoneIp, lanPin)}
                  className="flex-1 py-1.5 px-2 bg-gammaCyan text-black font-bold text-xs rounded-lg hover:bg-gammaCyan/80"
                >
                  {lanConnected ? 'Reconnect' : 'Pair & Connect'}
                </button>
                {lanConnected && (
                  <button
                    onClick={disconnectLanSync}
                    className="py-1.5 px-3 bg-red-500/20 text-red-400 text-xs rounded-lg border border-red-500/30"
                  >
                    Disconnect
                  </button>
                )}
              </div>

              {/* Direct LAN Audio Streaming Option */}
              <div className="p-3 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-mono text-slate-200">SPEAKER AUDIO OUTPUT</span>
                  <button
                    onClick={() => {
                      const next = !streamToPc;
                      setStreamToPc(next);
                      if (!next && pcAudioRef.current) pcAudioRef.current.pause();
                    }}
                    className={`text-xs px-2.5 py-1 rounded-md font-mono transition-colors ${
                      streamToPc ? 'bg-gammaCyan text-black font-bold' : 'bg-slate-800 text-slate-400 hover:text-slate-200'
                    }`}
                  >
                    {streamToPc ? '🔊 Stream to PC Speakers' : '📱 Phone Speaker Output'}
                  </button>
                </div>
                <p className="text-[10px] text-slate-500">
                  {streamToPc
                    ? 'Audio streams losslessly from phone to this PC via HTTP Range-request.'
                    : 'Standard remote control mode: Phone plays audio while PC views live telemetry & controls.'}
                </p>
              </div>

              {/* Two-Way Queue Push: Send Local File to Phone Queue */}
              <div className="p-3 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
                <span className="text-xs font-mono text-slate-200 block">PUSH AUDIO FILE TO PHONE QUEUE</span>
                <label className="flex items-center justify-center p-3 border border-dashed border-slate-700 hover:border-gammaCyan/60 rounded-xl cursor-pointer text-xs text-slate-400 hover:text-slate-200 transition-colors">
                  <input
                    type="file"
                    accept=".mp3,.flac,.wav,.m4a,.mp4"
                    disabled={isPushingTrack || !lanConnected}
                    onChange={(e) => {
                      const file = e.target.files?.[0];
                      if (file) uploadFileToPhoneQueue(file);
                    }}
                    className="hidden"
                  />
                  <span>{isPushingTrack ? 'Sending to phone...' : '📁 Select .mp3 / .flac to queue on phone'}</span>
                </label>
              </div>
            </div>

            <p className="text-[11px] text-slate-500">
              Zero cloud accounts required. GAMA's built-in WebSocket & HTTP streaming engine connects directly between your phone and PC over local Wi-Fi.
            </p>
          </div>
        </div>
      )}

      {/* Audio Engine for Direct Local / Stream / LAN Playback */}
      <audio
        ref={pcAudioRef}
        preload="metadata"
        className="hidden"
        onTimeUpdate={(e) => {
          if (!isUsingYouTubeRef.current) {
            setCurrentTime(e.target.currentTime);
            if (e.target.duration && !isNaN(e.target.duration)) {
              setDuration(e.target.duration);
            }
          }
        }}
        onPlaying={() => {
          if (!isUsingYouTubeRef.current) {
            setIsPlaying(true);
            setIsReconnectingStream(false);
            setReconnectAttempt(0);
            setPlaybackError(null);
          }
        }}
        onPause={() => {
          if (!isUsingYouTubeRef.current && !streamToPc) {
            setIsPlaying(false);
            clearTimeout(reconnectTimerRef.current);
            setIsReconnectingStream(false);
          }
        }}
        onEnded={() => {
          if (!isUsingYouTubeRef.current) {
            handleNext();
          }
        }}
        onStalled={() => {
          if ((currentTrack?.streamUrl || streamToPc) && isPlaying && !isReconnectingStream) {
            triggerStreamReconnect();
          }
        }}
        onError={(e) => {
          const err = e.target.error;
          if (err && err.code === 1) return; // MEDIA_ERR_ABORTED

          if ((currentTrack?.streamUrl || streamToPc) && isPlaying) {
            triggerStreamReconnect();
            return;
          }

          let msg = 'HTML5 audio element failed to load audio.';
          if (err) {
            switch (err.code) {
              case 2: // MEDIA_ERR_NETWORK
                msg = 'Network error: Failed to download audio stream or file.';
                break;
              case 3: // MEDIA_ERR_DECODE
                msg = 'Decode error: The audio file is corrupted or in an unsupported format.';
                break;
              case 4: // MEDIA_ERR_SRC_NOT_SUPPORTED
                msg = 'Source error: Format not supported, file missing, or HTTP 404/403.';
                break;
              default:
                msg = `Audio error code ${err.code}: ${err.message || 'Unknown playback error'}`;
                break;
            }
          }
          console.error('[GAMA Windows Audio] Error:', err, e.target.src);
          setPlaybackError({
            message: msg,
            url: e.target.src,
            canRetry: true
          });
          setIsPlaying(false);
        }}
      />
    </div>
  );
}
