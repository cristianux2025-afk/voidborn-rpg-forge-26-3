# Voidborn RPG: Cinematic Combat (Minecraft Forge 26.3)

Proyecto completo de Mod RPG para **Minecraft Forge 26.3** con soporte para **GeckoLib 4** y **PlayerAnimator**.

## Cómo compilar el archivo `.jar`

### Opción A: Compilar en tu PC (IntelliJ IDEA o Terminal)
1. Instala **Eclipse Temurin JDK 21** desde https://adoptium.net/temurin/releases/
2. Abre esta carpeta en **IntelliJ IDEA** (detectará `build.gradle` automáticamente y descargará Gradle y Forge) o abre una terminal con Gradle instalado y ejecuta:
   ```bash
   gradle build
   ```
3. El archivo `.jar` compilado aparecerá en `build/libs/`.

### Opción B: Compilar gratis en la nube sin instalar nada (GitHub Actions)
1. Sube esta carpeta a un repositorio de GitHub.
2. Ve a la pestaña **Actions** de tu repositorio: el flujo `.github/workflows/build.yml` compilará automáticamente el mod con JDK 21 y Gradle 8.8 y te dejará descargar el `.jar` listo en la sección **Artifacts**.
