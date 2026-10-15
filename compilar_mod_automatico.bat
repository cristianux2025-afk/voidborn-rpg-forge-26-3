@echo off
setlocal EnableDelayedExpansion
title Compilador Automatico - Voidborn RPG (Forge 26.3)
echo ============================================================================
echo   COMPILADOR AUTOMATICO DE VOIDBORN RPG (MINECRAFT FORGE 26.3)
echo ============================================================================
echo.

set "WORK_DIR=%~dp0.build_tools"
if not exist "%WORK_DIR%" mkdir "%WORK_DIR%"

echo [1/4] Verificando Java JDK 21...
if not exist "%WORK_DIR%\jdk21\bin\javac.exe" (
    echo Descargando Eclipse Temurin JDK 21 portable...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference = 'SilentlyContinue'; Invoke-WebRequest -Uri 'https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse' -OutFile '%WORK_DIR%\jdk21.zip'; Expand-Archive -Path '%WORK_DIR%\jdk21.zip' -DestinationPath '%WORK_DIR%\jdk_extract' -Force; $dir = Get-ChildItem '%WORK_DIR%\jdk_extract' | Select-Object -First 1; Move-Item $dir.FullName '%WORK_DIR%\jdk21' -Force"
)
set "JAVA_HOME=%WORK_DIR%\jdk21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
javac -version

echo.
echo [2/4] Verificando Gradle 8.8...
if not exist "%WORK_DIR%\gradle-8.8\bin\gradle.bat" (
    echo Descargando Gradle 8.8 oficial...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference = 'SilentlyContinue'; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.8-bin.zip' -OutFile '%WORK_DIR%\gradle.zip'; Expand-Archive -Path '%WORK_DIR%\gradle.zip' -DestinationPath '%WORK_DIR%' -Force"
)
set "PATH=%WORK_DIR%\gradle-8.8\bin;%PATH%"

echo.
echo [3/4] Compilando Voidborn RPG (.jar)... Esto puede tardar unos minutos la primera vez.
cd /d "%~dp0"
call gradle.bat build --no-daemon

echo.
echo [4/4] Copiando el archivo .jar a tu carpeta de Descargas...
set "DOWNLOADS=%USERPROFILE%\Downloads"
for %%f in ("build\libs\*.jar") do (
    copy /Y "%%f" "%DOWNLOADS%\voidbornrpg-1.0.0-forge-26.3.jar" >nul
    echo.
    echo ============================================================================
    echo   EXITO! Tu mod compilado esta listo en:
    echo   %DOWNLOADS%\voidbornrpg-1.0.0-forge-26.3.jar
    echo ============================================================================
)
pause
