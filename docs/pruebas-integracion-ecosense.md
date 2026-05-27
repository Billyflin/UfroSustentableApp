# Pruebas de integracion EcoSense

## 1. Estrategia de integracion

La estrategia elegida es **hibrida**.

Se usa un enfoque **bottom-up** para reutilizar servicios de dominio ya existentes, como `GrupoService` y `RankingService`, porque esas reglas ya estaban separadas de la UI y se podian validar sin emulador. Sobre ellos se agrega un enfoque **top-down** mediante servicios de aplicacion (`GrupoApplicationService` y `RecyclingApplicationService`) que coordinan repositorios, clientes externos y publicacion de eventos.

La prueba no entra por pantallas Compose ni usa emuladores, webdrivers o componentes visuales. El punto de entrada son servicios internos de aplicacion. Los modulos externos se reemplazan por dobles in-memory controlados:

- BD: repositorios in-memory de usuarios, grupos y solicitudes de reciclaje.
- Cliente externo: storage fake para subida de imagen.
- Cola/job: `EventPublisher` fake que registra mensajes publicados.

La estrategia permite validar colaboracion real entre modulos sin depender de Firebase ni red. En cada integracion se cubre al menos un camino correcto y un camino de error, verificando estado persistido y efectos laterales.

## 2. Diseno de pruebas

| ID / Titulo | Integracion | Precondiciones / Fixture | Entrada | Pasos | Resultados esperados |
|---|---|---|---|---|---|
| IT-01 - Crear grupo correctamente | `GrupoApplicationService` <-> `UsuarioRepositoryPort` <-> `GrupoRepositoryPort` <-> `EventPublisher` | Usuario `U001` existe; repositorio de grupos vacio; cola vacia | `crearGrupo("U001", "EcoLab", "Grupo de reciclaje", PUBLICO)` | Invocar servicio; persistir grupo; actualizar usuario; publicar evento | Grupo queda en BD; creador queda como administrador; usuario queda con `grupoId`; cola contiene `GROUP_CREATED` |
| IT-02 - Error 409 por grupo duplicado | `GrupoApplicationService` <-> repositorios <-> cola | Usuario `U001`; grupo existente `EcoLab`; cola vacia | `crearGrupo("U001", "EcoLab", "Duplicado", PUBLICO)` | Invocar servicio; validar duplicado antes de escribir | Retorna error `"Ya existe un grupo con ese nombre"`; BD conserva solo el grupo original; usuario no cambia; cola vacia |
| IT-03 - Unirse a grupo publico | `GrupoApplicationService` <-> `GrupoService` <-> repositorios <-> cola | Usuario `U002` con 30 puntos; grupo publico `G001` con 100 puntos | `unirseAGrupo("U002", "G001")` | Cargar usuario/grupo; ejecutar regla de dominio; guardar cambios; publicar evento | Usuario queda en `G001`; grupo contiene a `U002`; puntaje grupal sube a 130; cola contiene `GROUP_JOINED` |
| IT-04 - Error 404 al unirse a grupo inexistente | `GrupoApplicationService` <-> repositorios <-> cola | Usuario `U002`; repositorio de grupos vacio | `unirseAGrupo("U002", "G404")` | Buscar grupo; detectar ausencia; abortar | Retorna `"El grupo no existe"`; usuario sigue sin grupo; BD de grupos vacia; cola vacia |
| IT-05 - Crear solicitud de reciclaje | `RecyclingApplicationService` <-> storage <-> repositorio solicitudes <-> repositorio usuarios <-> cola | Usuario `U010`; storage fake exitoso; repositorio solicitudes vacio | `submitRequest("U010", "Plastico", 2.5, bytes, "Botellas PET")` | Subir imagen; crear solicitud; anexar historial; publicar job de validacion | Solicitud `REQ-1` queda en BD con `PROCESSING`; historial de usuario incluye `REQ-1`; cola contiene `RECYCLING_VALIDATION_REQUESTED` |
| IT-06 - Timeout del storage al crear solicitud | `RecyclingApplicationService` <-> storage <-> repositorios <-> cola | Usuario `U010`; storage fake lanza `"timeout al subir imagen"` | `submitRequest("U010", "Vidrio", 1.2, bytes, null)` | Intentar subir imagen; capturar fallo; abortar transaccion logica | Operacion falla; no hay solicitud en BD; historial no cambia; no se agenda job de validacion |
| IT-07 - Canjear recompensa | `RecyclingApplicationService` <-> repositorio solicitudes <-> repositorio usuarios <-> cola | Usuario `U020` con 10 puntos; solicitud `REQ-7` en estado `REWARD` | `redeemReward("REQ-7", "U020", 25)` | Buscar solicitud y usuario; actualizar estado; sumar puntos; publicar evento | Solicitud pasa a `REEDEMED`; reward queda en 25; usuario sube a 35 puntos; cola contiene `REWARD_REDEEMED` |
| IT-08 - Error 404 al canjear recompensa inexistente | `RecyclingApplicationService` <-> repositorios <-> cola | Usuario `U020` con 10 puntos; sin solicitud `REQ-404` | `redeemReward("REQ-404", "U020", 25)` | Buscar solicitud; detectar ausencia; abortar | Retorna `"Solicitud no encontrada"`; puntos siguen en 10; cola vacia |

## 3. Implementacion

Archivos agregados:

- `app/src/main/kotlin/com/ecosense/service/IntegrationPorts.kt`
- `app/src/main/kotlin/com/ecosense/service/GrupoApplicationService.kt`
- `app/src/main/kotlin/com/ecosense/service/RecyclingApplicationService.kt`
- `app/src/test/kotlin/com/ecosense/integration/EcoSenseIntegrationSpec.kt`
- `scripts/run-integration-tests.ps1`

Los asserts cubren:

- Estado en BD: mapas in-memory de usuarios, grupos, historial y solicitudes.
- Efectos laterales: eventos publicados en `RecordingEventPublisher`, que representa cola/job.
- Caminos de error: duplicado 409, no encontrado 404 y timeout del cliente storage.

## 4. Evidencia reproducible

Comando recomendado:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-integration-tests.ps1
```

Comando Gradle equivalente:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.ecosense.integration.EcoSenseIntegrationSpec" --rerun-tasks
```

Salida verificada en reporte JUnit XML:

```xml
<testsuite name="com.ecosense.integration.EcoSenseIntegrationSpec"
           tests="15"
           skipped="0"
           failures="0"
           errors="0"
           timestamp="2026-05-27T05:25:13.592Z"
           time="0.1">
```

Reporte local:

```text
app/build/reports/tests/testDebugUnitTest/index.html
app/build/test-results/testDebugUnitTest/TEST-com.ecosense.integration.EcoSenseIntegrationSpec.xml
```

Validacion adicional ejecutada:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Resultado: `BUILD SUCCESSFUL in 5s`.
