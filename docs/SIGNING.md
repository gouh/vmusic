# Firma de APK Release - VMusic

## Generar Keystore

Comando utilizado para generar la key de firma:

```bash
keytool -genkey -v -keystore vmusic-release-key.keystore -alias vmusic-key -keyalg RSA -keysize 2048 -validity 36500
```

- **Keystore:** `vmusic-release-key.keystore`
- **Alias:** `vmusic-key`
- **Validez:** 100 años (36500 días)

## Configuración del Proyecto

### 1. Crear archivo `android/key.properties`

```properties
storePassword=<tu_password>
keyPassword=<tu_password>
keyAlias=vmusic-key
storeFile=vmusic-release-key.keystore
```

### 2. Configurar `android/app/build.gradle.kts`

Agregar al inicio:

```kotlin
import java.util.Properties
import java.io.FileInputStream

val keystorePropertiesFile = rootProject.file("release.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}
```

Dentro del bloque `android`:

```kotlin
signingConfigs {
    create("release") {
        keyAlias = keystoreProperties["keyAlias"] as String
        keyPassword = keystoreProperties["keyPassword"] as String
        storeFile = file(keystoreProperties["storeFile"] as String)
        storePassword = keystoreProperties["storePassword"] as String
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

## Construir APK Release

```bash
cd android
./gradlew assembleRelease
```

El APK firmado se generará en: `android/app/build/outputs/apk/release/app-release.apk`

## Instalar APK en Dispositivo

Si hay problemas con Baseline Profile, desinstala la versión anterior primero:

```bash
adb uninstall mx.valdora.vmusic
adb install app/build/outputs/apk/release/app-release.apk
```

## Solución de Problemas

### Error: Keystore not found

Asegúrate de que el `storeFile` en `key.properties` apunte correctamente al keystore:
- Si está en la raíz del proyecto: `storeFile=../vmusic-release-key.keystore`
- Si está en `android/app/`: `storeFile=vmusic-release-key.keystore`

### Error: INSTALL_BASELINE_PROFILE_FAILED

Desinstala la app anterior antes de instalar:
```bash
adb uninstall mx.valdora.vmusic
```

## Importante

- **NO subir a git:** `key.properties` y `vmusic-release-key.keystore`
- Agregar al `.gitignore`:
  ```
  key.properties
  *.keystore
  ```
- Guardar el keystore en un lugar seguro
- Se necesita el mismo keystore para todas las actualizaciones futuras de la app
