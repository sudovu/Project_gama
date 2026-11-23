const { app, BrowserWindow, ipcMain, globalShortcut, shell } = require('electron');
const path = require('path');
const fs = require('fs');
const os = require('os');
const http = require('http');

let mainWindow;

function getAppDataDir() {
  const dir = path.join(app.getPath('appData'), 'GamaMusic');
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
  const dlDir = path.join(dir, 'downloads');
  if (!fs.existsSync(dlDir)) {
    fs.mkdirSync(dlDir, { recursive: true });
  }
  return dir;
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 840,
    minWidth: 960,
    minHeight: 640,
    backgroundColor: '#0a0a0f',
    title: 'GAMA - Cybernetic Music Platform',
    icon: fs.existsSync(path.join(__dirname, '../../build/icon.ico')) ? path.join(__dirname, '../../build/icon.ico') : undefined,
    webPreferences: {
      preload: path.join(__dirname, '../preload/preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      webSecurity: true
    }
  });

  // Remove default menu for clean, modern interface
  mainWindow.removeMenu();

  const isDev = process.env.NODE_ENV === 'development' || !app.isPackaged;
  const distIndex = path.join(__dirname, '../../dist/index.html');

  if (isDev && !fs.existsSync(distIndex)) {
    mainWindow.loadURL('http://localhost:5173');
  } else {
    mainWindow.loadFile(distIndex);
  }

  // Handle external links safely
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: 'deny' };
  });

  // Windows Media Keys Handling
  try {
    globalShortcut.register('MediaPlayPause', () => {
      mainWindow?.webContents.send('media-command', 'play-pause');
    });
    globalShortcut.register('MediaNextTrack', () => {
      mainWindow?.webContents.send('media-command', 'next');
    });
    globalShortcut.register('MediaPreviousTrack', () => {
      mainWindow?.webContents.send('media-command', 'previous');
    });
  } catch (e) {
    console.warn('Could not register media keys:', e);
  }
}

// IPC Handlers
ipcMain.handle('get-app-data-path', () => getAppDataDir());

ipcMain.handle('get-local-storage-data', (event, key) => {
  try {
    const file = path.join(getAppDataDir(), `${key}.json`);
    if (fs.existsSync(file)) {
      return JSON.parse(fs.readFileSync(file, 'utf-8'));
    }
    return null;
  } catch (e) {
    console.error(`Error reading ${key}:`, e);
    return null;
  }
});

ipcMain.handle('save-local-storage-data', (event, key, data) => {
  try {
    const file = path.join(getAppDataDir(), `${key}.json`);
    fs.writeFileSync(file, JSON.stringify(data, null, 2), 'utf-8');
    return true;
  } catch (e) {
    console.error(`Error writing ${key}:`, e);
    return false;
  }
});

ipcMain.handle('download-track', async (event, track) => {
  try {
    const dlPath = path.join(getAppDataDir(), 'downloads', `${track.id}.json`);
    fs.writeFileSync(dlPath, JSON.stringify({ ...track, downloadedAt: Date.now() }, null, 2), 'utf-8');
    return { success: true, localPath: dlPath };
  } catch (e) {
    return { success: false, error: e.message };
  }
});

ipcMain.handle('discover-lan-devices', async () => {
  const discovered = [];
  const interfaces = os.networkInterfaces();
  const subnets = new Set();

  for (const name of Object.keys(interfaces)) {
    for (const net of interfaces[name]) {
      if (net.family === 'IPv4' && !net.internal) {
        const parts = net.address.split('.');
        if (parts.length === 4) {
          subnets.add(`${parts[0]}.${parts[1]}.${parts[2]}`);
        }
      }
    }
  }

  const candidateIps = ['127.0.0.1'];
  for (const subnet of subnets) {
    for (let i = 1; i <= 254; i++) {
      candidateIps.push(`${subnet}.${i}`);
    }
  }

  const checkIp = (ip) => {
    return new Promise((resolve) => {
      const req = http.get(`http://${ip}:8765/status`, { timeout: 450 }, (res) => {
        if (res.statusCode === 200) {
          let rawData = '';
          res.on('data', (chunk) => { rawData += chunk; });
          res.on('end', () => {
            try {
              const data = JSON.parse(rawData);
              resolve({
                ip,
                port: 8765,
                deviceModel: data.deviceModel || 'GAMA Device',
                protocolVersion: data.protocolVersion || '1.2.0',
                pairingPin: data.pairingPin || '',
                trackTitle: data.track?.title || ''
              });
            } catch (_) {
              resolve(null);
            }
          });
        } else {
          resolve(null);
        }
      });
      req.on('error', () => resolve(null));
      req.on('timeout', () => { req.destroy(); resolve(null); });
    });
  };

  const chunkSize = 35;
  for (let i = 0; i < candidateIps.length; i += chunkSize) {
    const chunk = candidateIps.slice(i, i + chunkSize);
    const results = await Promise.all(chunk.map(checkIp));
    for (const res of results) {
      if (res && !discovered.some((d) => d.ip === res.ip)) {
        discovered.push(res);
      }
    }
    if (discovered.length > 0 && i >= 70) break;
  }

  return discovered;
});

app.whenReady().then(() => {
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('will-quit', () => {
  globalShortcut.unregisterAll();
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});
