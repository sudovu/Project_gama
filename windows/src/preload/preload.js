const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
  getAppDataPath: () => ipcRenderer.invoke('get-app-data-path'),
  getStorageData: (key) => ipcRenderer.invoke('get-local-storage-data', key),
  saveStorageData: (key, data) => ipcRenderer.invoke('save-local-storage-data', key, data),
  downloadTrack: (track) => ipcRenderer.invoke('download-track', track),
  onMediaCommand: (callback) => ipcRenderer.on('media-command', (event, cmd) => callback(cmd))
});
