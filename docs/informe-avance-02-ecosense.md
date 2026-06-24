# Entrega Avance 02 - Proyecto semestral EcoSense

**Curso:** Pruebas de software
**Equipo:** Billy Martinez, Bastian Lagos
**Fecha:** 24/06/2026
**Repositorio/rama:** `codex/performance-optimizations`

## Resumen ejecutivo

Este informe consolida la evidencia historica ya versionada en `docs/` y la evidencia nueva generada para Avance 02. No se interpreta solo la fotografia actual del proyecto: se registra la linea base de SonarQube, los hallazgos corregidos, la suite de integracion original y la ampliacion actual de BDD/cobertura.

El avance implementa y evidencia el bloque funcional RF10-RF17 asociado a grupos, ranking y recompensas. Este bloque cubre 8 casos de uso priorizados y se considera el 40% del alcance funcional planificado para esta entrega. La evidencia incluye pruebas unitarias con Kotest, escenarios BDD ejecutables con Cucumber/Gherkin, pruebas de integracion de servicios y reporte local de cobertura JaCoCo.

Resultado actual: `83` tests JVM ejecutados, `0` fallos, `0` errores y `0` omitidos. La cobertura local sobre el alcance configurado del avance es **89.23% lines** y **77.88% branches**.

## Linea base historica y evidencia previa

| Fecha | Evidencia del repo | Resultado documentado | Lectura para Avance 02 |
|---|---|---|---|
| 27/05/2026 | `docs/pruebas-integracion-ecosense.md` | Suite `EcoSenseIntegrationSpec` original con 15 tests, 0 failures, 0 errors; `BUILD SUCCESSFUL in 5s`. | Punto de partida de integracion entre servicios, repositorios in-memory, storage fake y eventos. |
| 27/05/2026 | `docs/informe-sonarqube-ecosense.md` + `docs/sonarqube-*.json` | Quality Gate OK, Bugs 0, Vulnerabilities 0, Duplications 0.0%, Coverage Sonar 0.0% por falta de XML JaCoCo. | La cobertura 0.0% era una limitacion de importacion, no ausencia de pruebas. |
| 27/05/2026 | `docs/sonarqube-issues.json` + `docs/sonarqube-resolved-issues.json` | Iteracion Sonar con 22 code smells detectados: 20 abiertos y 2 corregidos. | Permite mostrar antes/despues real: los issues corregidos ya no aparecen abiertos en el analisis posterior. |
| 01/06/2026 | `docs/diagramas-c4/*.puml` | Diagramas C4 de contexto, contenedores y componentes Android. | Base de arquitectura para mapear CU implementados. |
| 23/06/2026 | `docs/informe-rendimiento-ecosense.md` | APK release baja de 34.727.433 a 8.413.167 bytes y startup mediana baja de 5.596 ms a 3.842 ms. | Evidencia complementaria de calidad/performance del proyecto. |
| 24/06/2026 | Reporte actual Avance 02 | 83 tests JVM, BDD RF10-RF17, JaCoCo lines 89.23%, branches 77.88%. | Evidencia actual para el requisito de TDD/BDD/cobertura. |

## Alcance del avance 02

| CU/RF | Implementacion | Pruebas unitarias | BDD | Integracion |
|---|---|---|---|---|
| RF10 Ranking global | `RankingService` | `RF10RankingGlobalSpec` | `grupos_ranking_recompensas.feature` | IT-15 |
| RF11 Crear grupo | `GrupoService`, `GrupoApplicationService` | `RF11CrearGrupoSpec` | `grupos_ranking_recompensas.feature` | IT-01, IT-02, IT-09, IT-10 |
| RF12 Unirse a grupo | `GrupoService`, `GrupoApplicationService` | `RF12UnirseGrupoSpec` | `unirse_grupo.feature` | IT-03, IT-04, IT-11, IT-12 |
| RF13 Ranking interno | `RankingService` | `RF13RankingGrupoSpec` | `grupos_ranking_recompensas.feature` | IT-15 |
| RF14 Puntos grupales | `GrupoService`, `GrupoApplicationService` | `RF14PuntosGrupalesSpec` | `grupos_ranking_recompensas.feature` | IT-13, IT-14 |
| RF15 Recompensas grupales | `GrupoService` | `RF15RecompensasGrupalesSpec` | `grupos_ranking_recompensas.feature` | IT-13 |
| RF16 Administrar miembros | `GrupoService` | `RF16AdminGrupoSpec` | `grupos_ranking_recompensas.feature` | Cubierto por dominio + BDD |
| RF17 Ranking grupal | `RankingService` | `RF17RankingGrupalSpec` | `grupos_ranking_recompensas.feature` | IT-15 |

## Arquitectura implementada

La app mantiene una arquitectura por capas:

| Capa | Elementos | Responsabilidad |
|---|---|---|
| UI Android | Compose screens, Navigation3 | Interaccion de usuario y navegacion |
| ViewModel/Repository | `viewmodel/*`, `repository/*` | Adaptacion a Firebase, estado de pantalla y carga remota |
| Servicios de aplicacion | `GrupoApplicationService`, `RecyclingApplicationService` | Orquestacion entre repositorios, storage y eventos |
| Dominio puro | `GrupoService`, `RankingService`, modelos | Reglas testeables sin Android, Firebase ni red |
| Puertos/Fakes | `IntegrationPorts.kt`, dobles in-memory | Integracion reproducible sin infraestructura externa |

Diagramas C4 existentes: `docs/diagramas-c4/01-contexto-sistema.puml`, `02-contenedores.puml` y `03-componentes-android.puml`.

## Proceso BDD + TDD aplicado

### BDD

Escenarios ejecutables:

- `app/src/test/resources/features/unirse_grupo.feature`
- `app/src/test/resources/features/grupos_ranking_recompensas.feature`
- Glue code: `app/src/test/kotlin/steps/UnirseGrupoSteps.kt` y `app/src/test/kotlin/steps/GruposRankingSteps.kt`

Extracto:

```gherkin
Escenario: RF17 calcular ranking grupal
  Dado los siguientes grupos para ranking grupal:
    | id | nombre           | puntajeTotal |
    | G1 | EcoVerde         | 500          |
    | G2 | RecicladoresUFRO | 800          |
  Cuando calculo el ranking grupal
  Entonces el primer grupo del ranking grupal debe ser "G2"
```

### TDD

Se evidencian ciclos por comportamiento en los specs RF10-RF17:

| Ciclo | Red | Green | Refactor |
|---|---|---|---|
| Grupo/RF12 | Se especifican errores al unirse a grupos publicos, privados y repetidos | `GrupoService.unirseAGrupo` implementa reglas puras | `GrupoApplicationService` orquesta repositorios y eventos sin cambiar la regla |
| Ranking/RF10-RF17 | Specs ordenan usuarios/grupos y posiciones globales | `RankingService` implementa ordenamientos deterministas | BDD reutiliza el servicio y evita duplicar reglas |
| Integracion reciclaje | Casos IT-05 a IT-08 fuerzan storage, historial, eventos y errores | `RecyclingApplicationService` coordina ports in-memory | Nuevos casos IT-16 e IT-17 cubren validaciones negativas |

## Pruebas unitarias y cobertura

Comando ejecutado:

```powershell
.\gradlew.bat :app:jacocoDebugUnitTestReport --rerun-tasks --console=plain
```

Resultado:

| Indicador | Valor |
|---|---:|
| Suites JVM | 10 |
| Tests | 83 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 0 |
| Tiempo XML acumulado | 1.7 s |

Cobertura JaCoCo:

| Tipo | Cubierto | Perdido | Total | % |
|---|---:|---:|---:|---:|
| Lines | 265 | 32 | 297 | 89.23% |
| Branches | 81 | 23 | 104 | 77.88% |
| Instructions | 1661 | 509 | 2170 | 76.54% |

Archivos generados:

- `app/build/reports/jacoco/jacocoDebugUnitTestReport/jacocoDebugUnitTestReport.xml`
- `app/build/reports/jacoco/jacocoDebugUnitTestReport/html/index.html`
- `app/build/reports/tests/testDebugUnitTest/index.html`
- `app/build/reports/cucumber/cucumber-report.html`

## Pruebas de integracion

Las pruebas integran servicios de aplicacion con puertos in-memory:

- `GrupoApplicationService` con repositorios de usuarios/grupos y `EventPublisher`.
- `RecyclingApplicationService` con repositorio de solicitudes, storage fake, usuarios y eventos.

La suite `EcoSenseIntegrationSpec` contiene IT-01 a IT-17 y valida caminos correctos, conflictos, 404, timeouts, validaciones de entrada, ranking y efectos laterales.

## Calidad con SonarQube: evolucion antes/despues

La lectura correcta de SonarQube debe considerar la evidencia historica del repositorio. Si solo se mira una ejecucion posterior a correcciones, se pierde el contexto: dos issues ya fueron cerrados y por eso no aparecen como abiertos.

| Momento | Evidencia | Bugs | Vulnerabilities | Code smells | Coverage | Duplications | Quality Gate |
|---|---|---:|---:|---:|---:|---:|---|
| Linea base de la iteracion Sonar | `sonarqube-issues.json` + `sonarqube-resolved-issues.json` | 0 | 0 | 22 detectados (20 abiertos + 2 corregidos) | 0.0% | 0.0% | OK |
| Despues de correcciones documentadas | `sonarqube-metrics.json` + `informe-sonarqube-ecosense.md` | 0 | 0 | 20 abiertos | 0.0% | 0.0% | OK |
| Estado actual de Avance 02 | JaCoCo XML + `sonar-project.properties` | No reejecutado | No reejecutado | No reejecutado localmente | JaCoCo lines 89.23%, branches 77.88% | No reejecutado | Pendiente de reanalisis con Docker |

### Hallazgos Sonar corregidos

| Regla | Severidad | Archivo | Accion documentada |
|---|---|---|---|
| `kotlin:S1192` | CRITICAL | `app/src/main/kotlin/com/ecosense/service/GrupoApplicationService.kt` | Define a constant instead of duplicating this literal "Usuario no encontrado" 3 times. |
| `kotlin:S108` | MAJOR | `app/src/main/kotlin/com/ecosense/screen/RecycleFormScreen.kt` | Either remove or fill this block of code. |

### Hallazgos Sonar abiertos despues de correcciones

| Vista | Resultado |
|---|---:|
| Total abiertos | 20 |
| Critical | 6 |
| Major | 5 |
| Minor | 8 |
| Info | 1 |
| Complejidad cognitiva `kotlin:S3776` | 6 |
| Demasiados parametros `kotlin:S107` | 4 |
| Imports sin uso `kotlin:S1128` | 8 |

Accion tomada en este avance: se agrego `sonar.coverage.jacoco.xmlReportPaths` y se genero el XML JaCoCo. La re-ejecucion local de SonarQube no pudo completarse porque Docker Desktop no esta disponible en el entorno actual (`dockerDesktopLinuxEngine` no existe). El reporte local de cobertura queda listo para importarse en el proximo Sonar.

## Mejoras y deuda tecnica

| Prioridad | Mejora | Motivo |
|---|---|---|
| Alta | Re-ejecutar SonarQube con Docker Desktop activo | Actualizar dashboard con coverage JaCoCo real y conservar comparacion historica |
| Alta | Reducir complejidad de `RecycleFormScreen`, `MainActivity` y `GruposScreen` | Sonar mantiene hallazgos `kotlin:S3776` abiertos |
| Media | Reducir parametros en `AppNavHost`, `HistoryScreen` y `ProfileScreen` | Sonar mantiene hallazgos `kotlin:S107` abiertos |
| Media | Agregar tests instrumentados de UI minima | Cubrir flujos Compose no incluidos en JVM |
| Media | Automatizar reporte en CI | Evitar evidencia manual y regresiones |
| Baja | Grabar video final de evidencia | Entregable audiovisual requerido |

## Conclusiones

La entrega queda funcionalmente avanzada para el bloque RF10-RF17, con TDD/BDD ejecutable, integracion de servicios y cobertura local superior al 70% en lineas y ramas. La evidencia historica muestra que SonarQube detecto 22 code smells en la iteracion registrada, de los cuales 2 fueron corregidos y 20 quedaron como deuda tecnica priorizada. El principal pendiente externo es regenerar el dashboard SonarQube con Docker activo para importar el XML JaCoCo actual y grabar el video final de evidencia.

## Referencias y anexos

- Reporte de integracion: `docs/pruebas-integracion-ecosense.md`
- Reporte Sonar previo: `docs/informe-sonarqube-ecosense.md`
- Reporte rendimiento: `docs/informe-rendimiento-ecosense.md`
- Guion de video: `docs/guion-video-avance-02.md`
