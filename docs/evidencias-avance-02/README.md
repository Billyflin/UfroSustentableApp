# Evidencias Avance 02

Esta carpeta contiene salidas reproducibles para apoyar el informe y el video corto de la entrega.

| Archivo | Contenido |
|---|---|
| `tests-unitarios-integracion.log` | Ejecucion completa de `.\gradlew.bat :app:testDebugUnitTest --rerun-tasks --console=plain`. |
| `jacoco-gradle.log` | Ejecucion completa de `.\gradlew.bat :app:jacocoDebugUnitTestReport --rerun-tasks --console=plain`. |
| `jacoco-summary.txt` | Resumen extraido del XML JaCoCo: tests, failures, errors, lines, branches e instructions. |
| `sonarqube-run.log` | Intento real de ejecutar SonarQube local. Si Docker Desktop no esta activo, deja el error verificable. |

Los reportes HTML quedan en `app/build/reports/` y se regeneran con los comandos anteriores.
