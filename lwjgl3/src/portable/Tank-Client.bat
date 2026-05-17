@echo off
setlocal
set "ROOT=%~dp0"
set "JAVA_EXE=%ROOT%runtime\bin\java.exe"
set "APP_HOME=%ROOT%app"
set "ASSETS_DIR=%ROOT%assets"

if "%~1"=="" (
  echo Usage: Tank-Client.bat HOST_IP [--port PORT]
  echo Example: Tank-Client.bat 192.168.1.23
  pause
  exit /b 1
)

if not exist "%JAVA_EXE%" (
  echo Missing bundled Java runtime: %JAVA_EXE%
  pause
  exit /b 1
)

set "HOST_IP=%~1"
cd /d "%ASSETS_DIR%"
"%JAVA_EXE%" --enable-native-access=ALL-UNNAMED -cp "%APP_HOME%\lib\*" com.silence.tank.lwjgl3.Lwjgl3Launcher --connect "%HOST_IP%" %2 %3 %4 %5 %6 %7 %8 %9
endlocal
