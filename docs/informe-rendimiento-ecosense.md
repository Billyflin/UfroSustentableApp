# Informe de rendimiento EcoSense

Fecha de ejecucion: 23/06/2026
Rama: `codex/performance-optimizations`
Commit base medido: `38e767a`

## Resumen

Se aplicaron optimizaciones de build, arranque, UI Compose, camara QR, cache de consultas y subida de imagenes. El mayor impacto fue mover Macrobenchmark/Perfetto fuera del APK productivo: el APK release bajo de `34.727.433` bytes a `8.413.167` bytes.

| Indicador | Antes | Despues | Delta |
| --- | ---: | ---: | ---: |
| APK release unsigned | 34.727.433 bytes | 8.413.167 bytes | -26.314.266 bytes (-75,77%) |
| APK release unsigned | 33,12 MiB | 8,02 MiB | -25,10 MiB |
| Startup ADB mediana (`am start -W`) | 5.596 ms | 3.842 ms | -1.754 ms (-31,34%) |
| Tests JVM no cacheados | 35,99 s | 35,77 s | -0,22 s |
| `:app:testDebugUnitTest` | OK | OK | 67 tests OK |

## Cambios aplicados

- Se creo el modulo `:benchmark` con `com.android.test`, Macrobenchmark `1.4.1`, `AndroidBenchmarkRunner`, tests de startup cold/warm/hot y recorrido principal de UI.
- Se agrego `ProfileInstaller`, variante `benchmark` release-like y Baseline Profile generado en `app/src/main/baseline-prof.txt`.
- Se quito `androidx.benchmark.macro` del APK de la app; Perfetto y `trace_processor_shell_*` ya no se empaquetan en release.
- Se eliminaron dependencias directas no usadas por la app: ZXing, Gson, ML Kit text recognition, runtime-livedata y Google Fonts.
- QR scanner: analisis en executor dedicado, `STRATEGY_KEEP_ONLY_LATEST`, cierre explicito de scanner/camara y throttle para evitar lecturas Firestore repetidas del mismo QR.
- Recycle form: parseo QR memoizado, lista de materiales estable, compresion JPEG en dispatcher de fondo y fallback Firestore esperado con `await()`.
- Map/History/Rewards: cache de puntos de mapa, guardas para no recargar ViewModels ya inicializados y ordenamientos memoizados en Compose.

## Resultados de APK

### Antes

| Entrada principal | Tamano sin comprimir | Tamano comprimido |
| --- | ---: | ---: |
| `assets/trace_processor_shell_x86` | 11.999.808 | 4.286.994 |
| `assets/trace_processor_shell_x86_64` | 11.218.896 | 3.926.167 |
| `assets/trace_processor_shell_aarch64` | 10.799.128 | 3.774.318 |
| `assets/trace_processor_shell_arm` | 8.373.124 | 3.484.510 |
| `classes.dex` | 7.997.336 | 7.997.336 |
| `lib/arm64-v8a/libtracing_perfetto.so` | 3.807.496 | 3.807.496 |

### Despues

| Entrada principal | Tamano sin comprimir | Tamano comprimido |
| --- | ---: | ---: |
| `classes.dex` | 7.532.584 | 7.532.584 |
| `resources.arsc` | 270.060 | 270.060 |
| `lib/arm64-v8a/libdatastore_shared_counter.so` | 54.304 | 54.304 |
| `lib/armeabi-v7a/libdatastore_shared_counter.so` | 53.412 | 53.412 |
| `okhttp3/internal/publicsuffix/publicsuffixes.gz` | 41.394 | 41.409 |

## Runtime

### Startup ADB antes/despues

Medicion: 10 iteraciones con `adb shell am force-stop com.ecosense` y `adb shell am start -W -n com.ecosense/.MainActivity` sobre AVD `Medium_Phone`.

| Metrica `TotalTime` | Antes | Despues |
| --- | ---: | ---: |
| Min | 4.427 ms | 3.417 ms |
| Mediana | 5.596 ms | 3.842 ms |
| Max | 7.417 ms | 12.481 ms |

Nota: el maximo posterior incluye un outlier de primera corrida despues de instalar; la mediana mejora 31,34%.

### Macrobenchmark posterior

Macrobenchmark se agrego en esta rama, por lo que no existia una ejecucion formal comparable en el estado base. Los resultados siguientes quedan como nueva linea base reproducible. Se ejecuto en emulador, con `androidx.benchmark.suppressErrors=EMULATOR`, por lo que sirve para regression testing local, no como resultado representativo de dispositivo fisico.

| Benchmark | Metrica | Min | Mediana/P50 | Max/P99 |
| --- | --- | ---: | ---: | ---: |
| `startupCold` | `timeToInitialDisplayMs` | 2.646,52 ms | 3.256,65 ms | 5.017,81 ms |
| `startupWarm` | `timeToInitialDisplayMs` | 503,48 ms | 757,57 ms | 795,69 ms |
| `startupHot` | `timeToInitialDisplayMs` | 162,41 ms | 316,76 ms | 1.773,91 ms |
| `mainNavigationFrameTiming` | `frameDurationCpuMs` | - | P50 7,81 ms | P99 16,77 ms |
| `mainNavigationFrameTiming` | `frameOverrunMs` | - | P50 -5,23 ms | P99 22,65 ms |

## Comandos reproducibles

```powershell
.\gradlew.bat :app:testDebugUnitTest --rerun-tasks --console=plain
.\gradlew.bat :app:assembleRelease --rerun-tasks --console=plain
.\gradlew.bat :benchmark:connectedBenchmarkReleaseAndroidTest --console=plain
.\gradlew.bat :benchmark:collectNonMinifiedReleaseBaselineProfile --console=plain
```

Para la medicion ADB:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am force-stop com.ecosense
adb shell am start -W -n com.ecosense/.MainActivity
```

## Evidencia local

| Evidencia | Ruta |
| --- | --- |
| Tests antes | `build/performance-baseline/tests-before.log` |
| APK antes | `build/performance-baseline/apk-top-before.csv` |
| Startup ADB antes | `build/performance-baseline/startup-adb-before.csv` |
| Tests despues | `build/performance-after/tests-after-final.log` |
| APK despues | `build/performance-after/apk-top-after.csv` |
| Startup ADB despues | `build/performance-after/startup-adb-after.csv` |
| Macrobenchmark despues | `benchmark/build/outputs/connected_android_test_additional_output/benchmarkRelease/connected/Medium_Phone(AVD) - 17/com.ecosense.benchmark-benchmarkData.json` |

Los archivos bajo `build/` no se versionan; se regeneran con los comandos anteriores.
