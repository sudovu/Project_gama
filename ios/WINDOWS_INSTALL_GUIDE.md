# How to Install GAMA on iOS from a Windows Laptop

You **do not need a Mac** to install and run GAMA on your iPhone or iPad! 

Because Apple requires iOS apps to be signed before they can run on a physical device, this guide explains how to generate the `.ipa` package in the cloud for free using GitHub and install it directly onto your iPhone from your Windows laptop in under 5 minutes.

---

## ⚡ Quick 3-Step Overview

1. **Get the `.ipa` file** (Automated by GitHub Actions on GitHub's free macOS runners).
2. **Download Sideloadly on Windows** (The industry-standard free iOS installer for Windows).
3. **Plug your iPhone into Windows via USB** and click **Start** in Sideloadly.

---

## 📋 Prerequisites on Your Windows Laptop

1. **iTunes for Windows** (Required for Apple USB Drivers):
   - Download the official version directly from Apple (do **not** use the Microsoft Store version):
     - [iTunes 64-bit for Windows](https://www.apple.com/itunes/download/win64)
2. **Sideloadly for Windows** (Free):
   - Download from: [https://sideloadly.io](https://sideloadly.io)
3. **Your iPhone / iPad & Lightning/USB-C Cable**.
4. **Any standard Apple ID** (Free, no paid Apple Developer account needed).

---

## 🚀 Step-by-Step Installation Instructions

### Step 1: Download `GAMA-v1.0.ipa`
1. Go to your repository's GitHub Actions tab:  
   👉 [https://github.com/sudovu/Project_gama/actions](https://github.com/sudovu/Project_gama/actions)
2. Click on the latest workflow run: **"Build Multi-Platform Artifacts (iOS & Windows)"**.
3. Under the **Artifacts** section at the bottom of the page, click on **`GAMA-v1.0-iOS-IPA`** to download it to your Windows laptop.
4. Unzip the downloaded file to get `GAMA-v1.0.ipa`.

---

### Step 2: Connect iPhone to Your Windows Laptop
1. Connect your iPhone to your Windows laptop using your USB cable.
2. Unlock your iPhone. If a popup asks **"Trust This Computer?"**, tap **Trust** and enter your iPhone passcode.
3. Open **iTunes** once just to confirm your iPhone shows up in the top left bar.

---

### Step 3: Sideload with Sideloadly
1. Launch **Sideloadly** on your Windows laptop.
2. In Sideloadly, you should see your connected iPhone listed under **iDevice**.
3. Drag and drop the `GAMA-v1.0.ipa` file into the large **IPA icon** area on the left of Sideloadly.
4. Under **Apple ID**, type in your personal Apple ID email address (e.g. `yourname@gmail.com` or `yourname@icloud.com`).
   > *Note: Sideloadly communicates directly with Apple's servers to request a free 7-day development certificate for your device.*
5. Click the green **Start** button at the bottom.
6. Enter your Apple ID password if prompted (and the 2FA verification code that pops up on your iPhone).
7. Wait ~30 seconds until Sideloadly status displays: `Done!`. The **GAMA** app icon will appear on your iPhone home screen!

---

### Step 4: Trust the App on Your iPhone (First Time Only)
Before opening the app, Apple requires you to trust your own developer certificate:
1. On your iPhone, open **Settings**.
2. Go to **General** -> **VPN & Device Management** (or *Profiles & Device Management*).
3. Under **Developer App**, tap your Apple ID email.
4. Tap **Trust "[Your Apple ID]"** and tap **Trust** again to confirm.
5. *(iOS 16+ only)*: Go to **Settings** -> **Privacy & Security** -> scroll to the bottom -> enable **Developer Mode** and restart the device.

---

### 🎉 You're All Set!
Open **GAMA** on your iPhone! You now have:
- Full **background audio playback** (music keeps playing when locked).
- **Lock Screen & Dynamic Island** media controls.
- **5-Band Cybernetic Hardware DSP Equalizer**.
- Full commercial track soundscapes curated by **VHUWON MATHERS**.
