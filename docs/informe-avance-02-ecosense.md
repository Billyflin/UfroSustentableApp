# Entrega Avance 02 - Proyecto semestral EcoSense

**Curso:** Pruebas de software
**Equipo:** Billy Martinez, Bastian Lagos
**Fecha:** 24/06/2026
**Repositorio/rama:** `codex/performance-optimizations`

## Resumen ejecutivo

El avance implementa y evidencia el bloque funcional RF10-RF17 asociado a grupos, ranking y recompensas. Este bloque cubre 8 casos de uso priorizados y se considera el 40% del alcance funcional planificado para esta entrega. La evidencia incluye pruebas unitarias con Kotest, escenarios BDD ejecutables con Cucumber/Gherkin, pruebas de integracion de servicios y reporte local de cobertura JaCoCo.

Resultado actual: `83` tests JVM ejecutados, `0` fallos, `0` errores y `0` omitidos. La cobertura local sobre el alcance configurado del avance es **89.23% lines** y **77.88% branches**.

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

## Calidad con SonarQube

Evidencia previa en `docs/sonarqube-*.json`:

| Metrica | Resultado |
|---|---:|
| Quality Gate | OK |
| Bugs | 0 |
| Vulnerabilities | 0 |
| Code Smells | 20 |
| Coverage Sonar previo | 0.0% |
| Duplications | 0.0% |
| Maintainability rating | 1.0 |
| Security rating | 1.0 |
| Reliability rating | 1.0 |

Accion tomada en este avance: se agrego `sonar.coverage.jacoco.xmlReportPaths` y se genero el XML JaCoCo. La re-ejecucion local de SonarQube no pudo completarse porque Docker Desktop no esta disponible en el entorno actual (`dockerDesktopLinuxEngine` no existe). El reporte local de cobertura queda listo para importarse en el proximo Sonar.

## Mejoras y deuda tecnica

| Prioridad | Mejora | Motivo |
|---|---|---|
| Alta | Re-ejecutar SonarQube con Docker Desktop activo | Actualizar dashboard con coverage JaCoCo real |
| Alta | Reducir complejidad de `RecycleFormScreen` | Sonar marco complejidad cognitiva critica |
| Media | Reducir parametros en `AppNavHost` | Mejor mantenibilidad de navegacion |
| Media | Agregar tests instrumentados de UI minima | Cubrir flujos Compose no incluidos en JVM |
| Media | Automatizar reporte en CI | Evitar evidencia manual y regresiones |
| Baja | Grabar video final de evidencia | Entregable audiovisual requerido |

## Conclusiones

La entrega queda funcionalmente avanzada para el bloque RF10-RF17, con TDD/BDD ejecutable, integracion de servicios y cobertura local superior al 70% en lineas y ramas. El principal pendiente externo es regenerar el dashboard SonarQube con Docker activo y grabar el video final de evidencia.

## Referencias y anexos

- Reporte de integracion: `docs/pruebas-integracion-ecosense.md`
- Reporte Sonar previo: `docs/informe-sonarqube-ecosense.md`
- Reporte rendimiento: `docs/informe-rendimiento-ecosense.md`
- Guion de video: `docs/guion-video-avance-02.md`
