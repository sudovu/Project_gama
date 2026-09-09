const { app, BrowserWindow, ipcMain, globalShortcut, shell } = require('electron');
const path = require('path');
const fs = require('fs');

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
    icon: path.join(__dirname, '../../build/icon.ico'),
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
