# AgroTech AI - Master Startup Script
# This script starts the Flask backend and then builds/installs/launches the mobile app.

Write-Host "🌱 Starting AgroTech AI System Setup..." -ForegroundColor Cyan

# 1. Start Backend in background (same window)
Write-Host "🚀 Starting Backend Server (Flask) in background..." -ForegroundColor Yellow
Start-Process python -ArgumentList "main.py" -WorkingDirectory "c:\MY_PROJECTS\AgroTech AI\backend" -NoNewWindow

# 2. Build and Install Mobile App
Write-Host "📱 Building and Installing Mobile App..." -ForegroundColor Yellow
cd 'c:\MY_PROJECTS\AgroTech AI'
.\gradlew :mobile_app:installDebug

# 3. Launch the Mobile App
Write-Host "✨ Launching App on Device..." -ForegroundColor Green
adb shell am start -n com.agrotech.ai/com.agrotech.ai.ui.MainActivity

Write-Host "✅ Done! Backend is running and App is installed/launched." -ForegroundColor Cyan
