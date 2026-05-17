@echo off
setlocal
set "ROOT=%~dp0"
set "JAVA_EXE=%ROOT%runtime\bin\java.exe"
set "APP_HOME=%ROOT%app"
set "ASSETS_DIR=%ROOT%assets"

if not exist "%JAVA_EXE%" (
  echo Missing bundled Java runtime: %JAVA_EXE%
  pause
  exit /b 1
)

cd /d "%ASSETS_DIR%"
"%JAVA_EXE%" --enable-native-access=ALL-UNNAMED -cp "%APP_HOME%\lib\*" com.silence.tank.lwjgl3.Lwjgl3Launcher --host %*
endlocal
