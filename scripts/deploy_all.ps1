<#
.SYNOPSIS
    GAMA Automated Multi-Target Deployment Script
.DESCRIPTION
    Executes the standard deployment protocol whenever changes or version bumps are made:
    1. Compiles signed Android release APK
    2. Installs and launches on connected Android phone via ADB
    3. Builds Windows Desktop package (portable & NSIS installer)
    4. Deploys binaries to website repository (downloads/gamma/)
    5. Commits and pushes updates to website (gautambhuwan.com.np) and GAMA git repositories
#>

param(
    [string]$WebsiteRepoPath = "C:\Users\Sudo\gautambhuwan.com.np",
    [string]$DeviceSerial = "32101FDH3001V5",
    [string]$CommitMessage = ""
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
Set-Location $ProjectRoot

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  GAMA MULTI-TARGET DEPLOYMENT PIPELINE   " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Read Android Version
$gradleKts = Get-Content "app/build.gradle.kts" -Raw
$versionNameMatch = [regex]::Match($gradleKts, 'versionName\s*=\s*"([^"]+)"')
$versionCodeMatch = [regex]::Match($gradleKts, 'versionCode\s*=\s*(\d+)')
$androidVersion = if ($versionNameMatch.Success) { $versionNameMatch.Groups[1].Value } else { "latest" }
$androidCode = if ($versionCodeMatch.Success) { $versionCodeMatch.Groups[1].Value } else { "0" }

# 2. Read Windows Version
$pkgJson = Get-Content "windows/package.json" -Raw | ConvertFrom-Json
$winVersion = $pkgJson.version

Write-Host "Target Version: Android v$androidVersion (code $androidCode) | Windows v$winVersion" -ForegroundColor Yellow

# 3. Build Android Release APK
Write-Host "`n[1/5] Building Android Release APK..." -ForegroundColor Green
& ".\gradlew.bat" assembleRelease
$apkPath = "$ProjectRoot\app\build\outputs\apk\release\app-release.apk"
if (-not (Test-Path $apkPath)) {
    throw "Release APK build failed - file not found at $apkPath"
}
Write-Host "APK Built Successfully: $(Get-Item $apkPath | Select-Object -ExpandProperty Length) bytes" -ForegroundColor Green

# 4. Deploy to Phone
Write-Host "`n[2/5] Deploying to Android Phone ($DeviceSerial)..." -ForegroundColor Green
$adb = "C:\Users\Sudo\AppData\Local\Android\Sdk\platform-tools\adb.exe"
if (Test-Path $adb) {
    & $adb -s $DeviceSerial install -r $apkPath
    & $adb -s $DeviceSerial shell am start -n com.aistudio.gamma.freq/com.example.MainActivity
    Write-Host "Phone deployment complete." -ForegroundColor Green
} else {
    Write-Warning "ADB not found at $adb, skipping phone install."
}

# 5. Build Windows Desktop App
Write-Host "`n[3/5] Building Windows Desktop Executables..." -ForegroundColor Green
Push-Location "$ProjectRoot\windows"
npm run dist
Pop-Location

$winPortable = "$ProjectRoot\windows\release\GAMA $winVersion.exe"
$winSetup = "$ProjectRoot\windows\release\GAMA Setup $winVersion.exe"

# 6. Deploy to Website
Write-Host "`n[4/5] Deploying Binaries to Website ($WebsiteRepoPath)..." -ForegroundColor Green
if (Test-Path $WebsiteRepoPath) {
    $gammaDir = "$WebsiteRepoPath\downloads\gamma"
    if (-not (Test-Path $gammaDir)) { New-Item -ItemType Directory -Path $gammaDir -Force }
    
    # Copy APKs
    Copy-Item $apkPath -Destination "$gammaDir\GAMA-v$androidVersion.apk" -Force
    Copy-Item $apkPath -Destination "$gammaDir\GAMA-latest.apk" -Force
    
    # Copy Windows binaries
    if (Test-Path $winSetup) {
        Copy-Item $winSetup -Destination "$gammaDir\GAMA-Setup-$winVersion.exe" -Force
    }
    if (Test-Path $winPortable) {
        Copy-Item $winPortable -Destination "$gammaDir\GAMA-windows-portable.exe" -Force
    }

    Write-Host "Binaries staged in website downloads/gamma/." -ForegroundColor Green
    
    # Commit and push website
    Push-Location $WebsiteRepoPath
    git pull --rebase origin main
    git add developments.html downloads/gamma/
    $webMsg = if ($CommitMessage) { $CommitMessage } else { "Deploy GAMA v$androidVersion: automated multi-target release" }
    git commit -m $webMsg
    git push origin main
    Pop-Location
    Write-Host "Website updated and pushed." -ForegroundColor Green
}

# 7. Push GAMA Git Repository
Write-Host "`n[5/5] Committing & Pushing GAMA Git Repository..." -ForegroundColor Green
git add app/build.gradle.kts windows/package.json scripts/
$gitMsg = if ($CommitMessage) { $CommitMessage } else { "chore(release): v$androidVersion (code $androidCode) & windows v$winVersion" }
git commit -m $gitMsg
git push origin main
Write-Host "GAMA repository updated and pushed." -ForegroundColor Green

Write-Host "`nAll targets (Git, Website, Phone, PC) successfully updated to v$androidVersion!" -ForegroundColor Cyan
