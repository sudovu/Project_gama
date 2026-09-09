# GAMA - Cybernetic Music Platform (Windows Desktop)

A futuristic, high-performance desktop music player application engineered for Windows. Built using Electron 31, React 18, Vite 5, and Tailwind CSS.

---

## ⚡ Key Features

- **Cybernetic Neon Dark Aesthetic**: Engineered with GAMA's signature cybernetic visual identity (Deep Space `#0a0a0f`, Neon Cyan `#00ffcc`, Cyber Violet `#a855f7`, and Neon Pink `#ec4899`).
- **Curated Commercial Soundscape**: 20 pre-calibrated frequency-tuned tracks spanning Rock & Metal (Linkin Park, Metallica, Nirvana), Synthwave/Cyberpunk, Pop, Hip-Hop, and Focus frequencies.
- **Hardware Media Key Integration**: Global Windows shortcuts (`MediaPlayPause`, `MediaNextTrack`, `MediaPreviousTrack`) hooked via Electron IPC bridge.
- **5-Band DSP Equalizer**: Real-time audio tone shaping across 60Hz, 230Hz, 910Hz, 3.6kHz, and 14kHz with custom presets (*Flat*, *Bass Boost*, *Cyber Rock*, *Vocal Clarity*, *Electronic*).
- **Embedded Picture-in-Picture Visualizer**: Seamless YouTube IFrame playback engine with toggleable floating PiP canvas visualizer.
- **Local Storage & Offline Library**: Stores favorites, download metadata, and equalizer configurations in `%APPDATA%\GamaMusic`.
- **Developer Profile**: Dedicated bio and credentials for **VHUWON MATHERS** (*Lead Architect & Systems Engineer*).

---

## 🛠️ Prerequisites

- **OS**: Windows 10 / 11 (x64 / ARM64)
- **Node.js**: `v18.0.0` or higher (tested on Node.js `v24.20.0`)
- **npm**: `v9.0.0` or higher

---

## 🚀 Getting Started

### 1. Install Dependencies
```powershell
cd windows
npm install
```

### 2. Build the Renderer (Vite)
```powershell
npm run build:vite
```

### 3. Launch the Application
```powershell
npm start
```

### 4. Development Mode (Hot Reload)
```powershell
# Terminal 1: Run Vite dev server
npm run dev

# Terminal 2: Launch Electron pointing to dev server
$env:NODE_ENV="development"; npm start
```

### 5. Package for Windows Distribution (.exe / installer)
```powershell
npm run dist
```

---

## 📁 Architecture Overview

```
windows/
├── src/
│   ├── main/
│   │   └── main.js           # Electron main process, window creation, media keys, IPC
│   ├── preload/
│   │   └── preload.js        # Secure contextBridge exposing electronAPI to renderer
│   └── renderer/
│       ├── assets/           # Static assets (developer profile image)
│       ├── App.jsx           # Main React application & player coordinator
│       ├── main.jsx          # React DOM entry point
│       ├── index.html        # HTML root document
│       ├── mockData.js       # Curated track frequencies and metadata
│       └── styles.css        # Tailwind CSS and cybernetic utility classes
├── build/                    # App icons and packaging resources
├── package.json              # Project manifests and scripts
├── vite.config.js            # Vite build configuration
└── tailwind.config.js        # Cybernetic theme extensions
```

---

## 👤 Developer

**VHUWON MATHERS**  
*Lead Architect & Systems Engineer*  
GitHub: [https://github.com/sudovu](https://github.com/sudovu)  
Project Repository: [https://github.com/sudovu/Project_gama](https://github.com/sudovu/Project_gama)
