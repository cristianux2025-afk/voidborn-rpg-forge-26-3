#!/usr/bin/env bash
set -e
echo "============================================================================"
echo "  COMPILADOR AUTOMATICO DE VOIDBORN RPG (MINECRAFT FORGE 26.3)"
echo "============================================================================"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORK_DIR="$SCRIPT_DIR/.build_tools"
mkdir -p "$WORK_DIR"

if [ ! -f "$WORK_DIR/jdk21/bin/javac" ]; then
    echo "[1/4] Descargando Eclipse Temurin JDK 21 portable..."
    OS_TYPE="linux"
    if [[ "$OSTYPE" == "darwin"* ]]; then OS_TYPE="mac"; fi
    ARCH_TYPE="x64"
    if [[ "$(uname -m)" == "arm64" || "$(uname -m)" == "aarch64" ]]; then ARCH_TYPE="aarch64"; fi
    curl -L "https://api.adoptium.net/v3/binary/latest/21/ga/${OS_TYPE}/${ARCH_TYPE}/jdk/hotspot/normal/eclipse" -o "$WORK_DIR/jdk21.tar.gz"
    mkdir -p "$WORK_DIR/jdk21"
    tar -xzf "$WORK_DIR/jdk21.tar.gz" -C "$WORK_DIR/jdk21" --strip-components=1
fi
export JAVA_HOME="$WORK_DIR/jdk21"
export PATH="$JAVA_HOME/bin:$PATH"

if [ ! -f "$WORK_DIR/gradle-8.8/bin/gradle" ]; then
    echo "[2/4] Descargando Gradle 8.8..."
    curl -L "https://services.gradle.org/distributions/gradle-8.8-bin.zip" -o "$WORK_DIR/gradle.zip"
    unzip -q -o "$WORK_DIR/gradle.zip" -d "$WORK_DIR"
fi
export PATH="$WORK_DIR/gradle-8.8/bin:$PATH"

echo "[3/4] Compilando Voidborn RPG (.jar)..."
cd "$SCRIPT_DIR"
gradle build --no-daemon

DOWNLOADS_DIR="$HOME/Downloads"
mkdir -p "$DOWNLOADS_DIR"
cp build/libs/*.jar "$DOWNLOADS_DIR/voidbornrpg-1.0.0-forge-26.3.jar"
echo "============================================================================"
echo "  EXITO! Tu mod compilado esta listo en:"
echo "  $DOWNLOADS_DIR/voidbornrpg-1.0.0-forge-26.3.jar"
echo "============================================================================"
