@echo off
echo 🌱 Starting AgroTech AI System Setup...

:: Set stable Java version from Android Studio to avoid Java 25 errors
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo ☕ Using Java from: %JAVA_HOME%

:: Stop any existing gradle daemons that might be using the wrong Java version
call gradlew.bat --stop >nul 2>&1

:: 1. Kill any existing backend on port 5000
echo 🔍 Cleaning up previous backend instances...
taskkill /F /IM python.exe /T >nul 2>&1

echo 📦 Checking and installing AI dependencies...
python -m pip install onnxruntime numpy pillow cloudinary flask flask-cors requests pymongo python-dotenv

echo 🛡️ Opening Port 5000 in Windows Firewall for Mobile Connection...
netsh advfirewall firewall add rule name="AgroTech_Backend" dir=in action=allow protocol=TCP localport=5000 >nul 2>&1

echo 🚀 Starting Backend Server (Flask) in background...
adb reverse tcp:5000 tcp:5000
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
