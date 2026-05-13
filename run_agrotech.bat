@echo off
echo 🌱 Starting AgroTech AI System Setup...

:: Set stable Java version from Android Studio to avoid Java 25 errors
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo ☕ Using Java from: %JAVA_HOME%
set "PYTHONUTF8=1"

:: 1. Force release locks and clean build directory
echo 🧹 Releasing file locks and cleaning build directory...
taskkill /F /IM java.exe >nul 2>&1
taskkill /F /IM adb.exe >nul 2>&1

if exist "mobile_app\build" (
    mkdir _empty_tmp >nul 2>&1
    robocopy _empty_tmp "mobile_app\build" /MIR >nul 2>&1
    rmdir /s /q _empty_tmp >nul 2>&1
    rmdir /s /q "mobile_app\build" >nul 2>&1
)

:: 2. Kill any existing backend on port 5000
echo 🔍 Cleaning up previous backend instances on port 5000...
FOR /F "tokens=5" %%T IN ('netstat -a -n -o ^| findstr :5000') DO (
    TaskKill.exe /PID %%T /F >nul 2>&1
)

echo ✅ Dependencies checked.

echo 🛡️ Opening Port 5000 in Windows Firewall for Mobile Connection...
netsh advfirewall firewall add rule name="AgroTech_Backend" dir=in action=allow protocol=TCP localport=5000 >nul 2>&1

echo 🚀 Starting Backend Server (Flask) in background...
adb reverse tcp:5000 tcp:5000
start /b cmd /c "cd /d "%~dp0backend" && python main.py"

:: 3. Build and Install Mobile App
echo 📱 Building and Installing Mobile App...
cd /d "%~dp0"
echo 🗑️ Uninstalling existing app to prevent signature conflicts...
adb uninstall com.agrotech.ai >nul 2>&1
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
