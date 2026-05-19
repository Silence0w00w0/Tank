@echo off
chcp 65001 >nul
setlocal
set "ROOT=%~dp0"
set "JAVA_EXE=%ROOT%runtime\bin\java.exe"
set "APP_HOME=%ROOT%app"
set "ASSETS_DIR=%ROOT%assets"

if "%~1"=="" (
  echo 用法：Tank-Client.bat 主机IP [--port 端口] [--auto]
  echo 示例：Tank-Client.bat 192.168.1.23
  pause
  exit /b 1
)

if not exist "%JAVA_EXE%" (
  echo 缺少内置 Java 运行时：%JAVA_EXE%
  pause
  exit /b 1
)

set "HOST_IP=%~1"
cd /d "%ASSETS_DIR%"
"%JAVA_EXE%" --enable-native-access=ALL-UNNAMED -cp "%APP_HOME%\lib\*" com.silence.tank.lwjgl3.Lwjgl3Launcher --connect "%HOST_IP%" %2 %3 %4 %5 %6 %7 %8 %9
endlocal
