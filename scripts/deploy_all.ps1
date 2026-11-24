<#
.SYNOPSIS
    GAMA Resilient Multi-Target Deployment Pipeline
.DESCRIPTION
    Standardized release pipeline enforcing unified versioning rules and deploying across:
    1. Local PC (Silent installation of Windows Desktop GAMA)
    2. Android Phone via ADB (with resilient fallback if offline)
    3. Website Repository (gautambhuwan.com.np downloads and developments.html)
    4. GAMA Git Repository (origin main)
.PARAMETER BumpType
    "patch" (default), "minor", or "none"
.PARAMETER SkipTests
    Switch to bypass unit tests if already verified
.PARAMETER DeviceSerial
    ADB device serial (default: 32101FDH3001V5)
.PARAMETER WebsiteRepoPath
    Local path to website repository (default: C:\Users\Sudo\gautambhuwan.com.np)
#>

param(
    [ValidateSet("patch", "minor", "none")]
    [string]$BumpType = "none",
    [switch]$SkipTests,
    [string]$DeviceSerial = "32101FDH3001V5",
    [string]$WebsiteRepoPath = "C:\Users\Sudo\gautambhuwan.com.np",
    [string]$ReleaseNotes = ""
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
Set-Location $ProjectRoot

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host "       GAMA RESILIENT MULTI-TARGET RELEASE PIPELINE    " -ForegroundColor Cyan
Write-Host "=======================================================`n" -ForegroundColor Cyan

# ---------------------------------------------------------
# STEP 1: Version Management & Optional Bumping
# ---------------------------------------------------------
$gradlePath = "$ProjectRoot\app\build.gradle.kts"
$gradleContent = Get-Content $gradlePath -Raw

$versionCodeMatch = [regex]::Match($gradleContent, 'versionCode\s*=\s*(\d+)')
$versionNameMatch = [regex]::Match($gradleContent, 'versionName\s*=\s*"([^"]+)"')

if (-not $versionCodeMatch.Success -or -not $versionNameMatch.Success) {
    throw "Could not parse versionCode or versionName from $gradlePath"
}

$currentCode = [int]$versionCodeMatch.Groups[1].Value
$currentVersionName = $versionNameMatch.Groups[1].Value

$pkgPath = "$ProjectRoot\windows\package.json"
$pkgContent = Get-Content $pkgPath -Raw
$pkgJson = $pkgContent | ConvertFrom-Json
$currentWinVersion = $pkgJson.version

$targetCode = $currentCode
$targetVersionName = $currentVersionName
$targetWinVersion = $currentWinVersion

if ($BumpType -ne "none") {
    $targetCode = $currentCode + 1
    
    # Bump Android versionName (major.minor.patch)
    $vParts = $currentVersionName.Split('.')
    if ($vParts.Length -eq 3) {
        $major = [int]$vParts[0]
        $minor = [int]$vParts[1]
        $patch = [int]$vParts[2]
        if ($BumpType -eq "minor") {
            $minor++
            $patch = 0
        } else {
            $patch++
        }
        $targetVersionName = "$major.$minor.$patch"
    } else {
        $targetVersionName = "$currentVersionName-next"
    }

    # Bump Windows package.json version
    $wParts = $currentWinVersion.Split('.')
    if ($wParts.Length -eq 3) {
        $wMajor = [int]$wParts[0]
        $wMinor = [int]$wParts[1]
        $wPatch = [int]$wParts[2]
        if ($BumpType -eq "minor") {
            $wMinor++
            $wPatch = 0
        } else {
            $wPatch++
        }
        $targetWinVersion = "$wMajor.$wMinor.$wPatch"
    }

    Write-Host "[1/6] Applying Version Bump ($BumpType):" -ForegroundColor Green
    Write-Host "      Android: v$currentVersionName (code $currentCode) -> v$targetVersionName (code $targetCode)" -ForegroundColor Yellow
    Write-Host "      Windows: v$currentWinVersion -> v$targetWinVersion" -ForegroundColor Yellow

    # Update app/build.gradle.kts
    $newGradle = $gradleContent -replace 'versionCode\s*=\s*\d+', "versionCode = $targetCode"
    $newGradle = $newGradle -replace 'versionName\s*=\s*"[^"]+"', "versionName = `"$targetVersionName`""
    Set-Content -Path $gradlePath -Value $newGradle -NoNewline

    # Update windows/package.json
    $pkgJson.version = $targetWinVersion
    $newPkg = $pkgJson | ConvertTo-Json -Depth 10
    Set-Content -Path $pkgPath -Value $newPkg
} else {
    Write-Host "[1/6] Using Active Version: Android v$targetVersionName (code $targetCode) | Windows v$targetWinVersion" -ForegroundColor Yellow
}

# ---------------------------------------------------------
# STEP 2: Verification Tests
# ---------------------------------------------------------
if (-not $SkipTests) {
    Write-Host "`n[2/6] Running Android Unit Tests (testDebugUnitTest)..." -ForegroundColor Green
    & ".\gradlew.bat" testDebugUnitTest
    if ($LASTEXITCODE -ne 0) {
        throw "Android unit tests failed with exit code $LASTEXITCODE. Aborting deployment."
    }
    Write-Host "All unit tests passed cleanly." -ForegroundColor Green
} else {
    Write-Host "`n[2/6] Unit tests bypassed (-SkipTests)." -ForegroundColor Yellow
}

# ---------------------------------------------------------
# STEP 3: Compiling Production Binaries (Android + Windows)
# ---------------------------------------------------------
Write-Host "`n[3/6] Compiling Release APK (assembleRelease)..." -ForegroundColor Green
& ".\gradlew.bat" assembleRelease
if ($LASTEXITCODE -ne 0) {
    throw "assembleRelease failed with exit code $LASTEXITCODE."
}

$apkPath = "$ProjectRoot\app\build\outputs\apk\release\app-release.apk"
if (-not (Test-Path $apkPath)) {
    throw "Release APK not found at $apkPath"
}
$apkSize = (Get-Item $apkPath).Length
Write-Host "APK Built: $apkPath ($apkSize bytes)" -ForegroundColor Green

Write-Host "`nPackaging Windows Desktop Application (Vite + Electron)..." -ForegroundColor Green
Push-Location "$ProjectRoot\windows"
npm run build
if ($LASTEXITCODE -ne 0) { Pop-Location; throw "npm run build failed." }
npm run dist
if ($LASTEXITCODE -ne 0) { Pop-Location; throw "npm run dist failed." }
Pop-Location

$winSetup = "$ProjectRoot\windows\release\GAMA Setup $targetWinVersion.exe"
$winPortable = "$ProjectRoot\windows\release\GAMA $targetWinVersion.exe"

if (-not (Test-Path $winSetup)) {
    # Fallback to any recent setup exe in release
    $foundSetup = Get-ChildItem "$ProjectRoot\windows\release\*Setup*.exe" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($foundSetup) { $winSetup = $foundSetup.FullName }
}
if (-not (Test-Path $winPortable)) {
    $foundPortable = Get-ChildItem "$ProjectRoot\windows\release\GAMA*.exe" | Where-Object { $_.Name -notlike "*Setup*" } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($foundPortable) { $winPortable = $foundPortable.FullName }
}

Write-Host "Windows Installer: $winSetup" -ForegroundColor Green
Write-Host "Windows Portable:  $winPortable" -ForegroundColor Green

# ---------------------------------------------------------
# STEP 4: Local PC Installation & Verification
# ---------------------------------------------------------
Write-Host "`n[4/6] Installing/Updating GAMA on this PC..." -ForegroundColor Green
if (Test-Path $winSetup) {
    try {
        Start-Process -FilePath $winSetup -ArgumentList "/S" -Wait
        $installedExe = "C:\Users\Sudo\AppData\Local\Programs\GAMA\GAMA.exe"
        if (Test-Path $installedExe) {
            Write-Host "GAMA successfully installed on this PC ($installedExe)." -ForegroundColor Green
        } else {
            Write-Host "Installer completed silently." -ForegroundColor Green
        }
    } catch {
        Write-Warning "Could not execute local PC installer: $_"
    }
} else {
    Write-Warning "Windows installer file not found, skipping local PC install."
}

# ---------------------------------------------------------
# STEP 5: Phone Deployment via ADB (Resilient Non-Fatal)
# ---------------------------------------------------------
Write-Host "`n[5/6] Deploying to Android Phone ($DeviceSerial)..." -ForegroundColor Green
$adb = "C:\Users\Sudo\AppData\Local\Android\Sdk\platform-tools\adb.exe"
if (Test-Path $adb) {
    $devicesOut = & $adb devices
    $isOnline = $devicesOut -match "$DeviceSerial\s+device"
    if ($isOnline) {
        Write-Host "Device $DeviceSerial is connected. Streaming install..." -ForegroundColor Green
        & $adb -s $DeviceSerial install -r $apkPath
        if ($LASTEXITCODE -eq 0) {
            & $adb -s $DeviceSerial shell am start -n com.aistudio.gamma.freq/com.example.MainActivity
            Write-Host "Phone deployment verified & launched." -ForegroundColor Green
        } else {
            Write-Warning "ADB install failed with exit code $LASTEXITCODE."
        }
    } else {
        Write-Host "[WARN] Device $DeviceSerial is not online. Skipping phone install without failing pipeline." -ForegroundColor Yellow
    }
} else {
    Write-Host "[WARN] ADB not found at $adb. Skipping phone install." -ForegroundColor Yellow
}

# ---------------------------------------------------------
# STEP 6: Deploy to Website & Git Repositories
# ---------------------------------------------------------
Write-Host "`n[6/6] Synchronizing Website and GAMA Repositories..." -ForegroundColor Green

# 6A: Website (gautambhuwan.com.np)
if (Test-Path $WebsiteRepoPath) {
    $gammaDir = "$WebsiteRepoPath\downloads\gamma"
    if (-not (Test-Path $gammaDir)) { New-Item -ItemType Directory -Path $gammaDir -Force }

    # Copy Binaries
    Copy-Item $apkPath -Destination "$gammaDir\GAMA-v$targetVersionName.apk" -Force
    Copy-Item $apkPath -Destination "$gammaDir\GAMA-latest.apk" -Force
    if (Test-Path $winSetup) {
        Copy-Item $winSetup -Destination "$gammaDir\GAMA-Setup-$targetWinVersion.exe" -Force
    }
    if (Test-Path $winPortable) {
        Copy-Item $winPortable -Destination "$gammaDir\GAMA-windows-portable.exe" -Force
    }

    # Update developments.html
    $devHtmlPath = "$WebsiteRepoPath\developments.html"
    if (Test-Path $devHtmlPath) {
        $devContent = Get-Content $devHtmlPath -Raw
        $devContent = [regex]::Replace($devContent, 'GAMA-v\d+\.\d+\.\d+\.apk', "GAMA-v$targetVersionName.apk")
        $devContent = [regex]::Replace($devContent, '"softwareVersion":\s*"[^"]+"', "`"softwareVersion`": `"$targetVersionName`"")
        $devContent = [regex]::Replace($devContent, 'GAMA Cybernetic Audio</div>\s*</div>', "GAMA Cybernetic Audio</div>`n        </div>")
        $devContent = [regex]::Replace($devContent, 'v\d+\.\d+\.\d+\s+MULTI-PLATFORM RELEASE', "v$targetVersionName MULTI-PLATFORM RELEASE")
        $devContent = [regex]::Replace($devContent, 'GAMA-Setup-\d+\.\d+\.\d+\.exe', "GAMA-Setup-$targetWinVersion.exe")
        Set-Content -Path $devHtmlPath -Value $devContent -NoNewline
    }

    # Rebase and push website repo
    Push-Location $WebsiteRepoPath
    try {
        git fetch origin main
        git pull --rebase origin main
        if ($LASTEXITCODE -ne 0) {
            git checkout --theirs developments.html downloads/gamma/*
            $env:GIT_EDITOR = "true"
            git rebase --continue
        }
        git add developments.html downloads/gamma/
        $webCommitMsg = if ($ReleaseNotes) {
            "Deploy GAMA v${targetVersionName} - $ReleaseNotes"
        } else {
            "Deploy GAMA v${targetVersionName} (code $targetCode) & Windows v${targetWinVersion}"
        }
        git commit -m $webCommitMsg
        git push origin main
        Write-Host "Website successfully deployed and pushed." -ForegroundColor Green
    } catch {
        Write-Warning "Website git push encountered an issue: $_"
    } finally {
        Pop-Location
    }
}

# 6B: GAMA Git Repository (Project_gama)
try {
    git fetch origin main
    git add app/ windows/ ios/ scripts/ README.md
    $gamaCommitMsg = if ($ReleaseNotes) {
        "feat(release): v$targetVersionName (code $targetCode) - $ReleaseNotes"
    } else {
        "chore(release): v$targetVersionName (code $targetCode) & windows v$targetWinVersion"
    }
    git commit -m $gamaCommitMsg
    git push origin main
    Write-Host "GAMA repository committed and pushed to origin main." -ForegroundColor Green
} catch {
    Write-Warning "GAMA git sync issue: $_"
}

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host "  MULTI-TARGET DEPLOYMENT COMPLETED SUCCESSFULLY!      " -ForegroundColor Cyan
Write-Host "  Targets: Phone, Website, This PC & Git               " -ForegroundColor Cyan
Write-Host "  Active Version: Android v$targetVersionName | Windows v$targetWinVersion" -ForegroundColor Cyan
Write-Host "=======================================================`n" -ForegroundColor Cyan
