@echo off
echo 🌱 Starting AgroTech AI System Setup...

:: 1. Start Backend in background (same window)
echo 🚀 Starting Backend Server (Flask) in background...
start /b cmd /c "cd /d c:\MY_PROJECTS\AgroTech AI\backend && python main.py"

:: 2. Build and Install Mobile App
echo 📱 Building and Installing Mobile App...
cd /d "c:\MY_PROJECTS\AgroTech AI"
call gradlew.bat :mobile_app:installDebug

:: 3. Launch the Mobile App
echo ✨ Launching App on Device...

:: Try to get the first device ID to avoid "more than one device" error
for /f "tokens=1" %%i in ('adb devices ^| findstr /v "List" ^| findstr /v "offline" ^| findstr "device$"') do (
    set DEVICE_ID=%%i
    goto :launch
)

:launch
if defined DEVICE_ID (
    echo 🎯 Targeting Device: %DEVICE_ID%
    adb -s %DEVICE_ID% shell am start -n com.agrotech.ai/com.agrotech.ai.ui.MainActivity
) else (
    echo ⚠️ No active device found. Trying default launch...
    adb shell am start -n com.agrotech.ai/com.agrotech.ai.ui.MainActivity
)

echo ✅ Done! Backend is running and App is installed/launched.
pause
