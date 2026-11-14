$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
Write-Host 'SangIT Android Agent v0.2: preflight Android build' -ForegroundColor Cyan
& java -version
if ($LASTEXITCODE -ne 0) { throw 'Java chua san sang: can JDK 17.' }
$gradle = Get-Command gradle -ErrorAction SilentlyContinue
if (-not $gradle) { throw 'Thieu Gradle 8.9. Cai qua Android Studio/Gradle hoac dung GitHub Actions trong source.' }
if (-not $env:ANDROID_HOME -and -not $env:ANDROID_SDK_ROOT) { throw 'Chua co ANDROID_HOME / ANDROID_SDK_ROOT. Cau hinh Android SDK 35 truoc.' }
& gradle --no-daemon :app:assembleDebug
if ($LASTEXITCODE -ne 0) { throw 'Android build FAILED. Khong duoc su dung APK.' }
$apk = Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
if (-not (Test-Path $apk)) { throw 'Build reported success but APK not found.' }
$hash = Get-FileHash -Algorithm SHA256 -Path $apk
Write-Host "APK: $apk"
Write-Host "SHA256: $($hash.Hash)" -ForegroundColor Green
