# Guion video Avance 02 EcoSense

Duracion objetivo: 4 a 5 minutos.

Este guion asume grabacion local con Docker Desktop activo solo si se quiere mostrar el dashboard SonarQube actualizado. Si Docker no esta activo, usar `docs/evidencias-avance-02/sonarqube-run.log` como evidencia de limitacion y mostrar los JSON historicos versionados.

1. Mostrar rama, commit y estado:

```powershell
git status --short --branch
git log --oneline -5
```

2. Ejecutar pruebas unitarias, BDD e integracion:

```powershell
.\gradlew.bat :app:testDebugUnitTest --rerun-tasks --console=plain
```

3. Ejecutar cobertura JaCoCo:

```powershell
.\gradlew.bat :app:jacocoDebugUnitTestReport --rerun-tasks --console=plain
```

4. Abrir reportes locales:

```powershell
start app\build\reports\tests\testDebugUnitTest\index.html
start app\build\reports\cucumber\cucumber-report.html
start app\build\reports\jacoco\jacocoDebugUnitTestReport\html\index.html
```

5. Mostrar resumen versionado para no depender solo de pantalla:

```powershell
Get-Content docs\evidencias-avance-02\jacoco-summary.txt
Get-Content docs\evidencias-avance-02\tests-unitarios-integracion.log -Tail 20
```

6. Mostrar evidencia Sonar previa y explicar que el dashboard se regenera con Docker Desktop activo:

```powershell
Get-Content docs\informe-sonarqube-ecosense.md
Get-Content docs\sonarqube-qualitygate.json
Get-Content docs\sonarqube-metrics.json
Get-Content docs\sonarqube-resolved-issues.json
powershell -ExecutionPolicy Bypass -File .\scripts\run-sonarqube-analysis.ps1
```

Si Docker no esta activo:

```powershell
Get-Content docs\evidencias-avance-02\sonarqube-run.log
```

7. Mostrar evidencia historica complementaria:

```powershell
Get-Content docs\pruebas-integracion-ecosense.md
Get-Content docs\diagramas-c4\README.md
```

8. Mostrar el informe final:

```powershell
start docs\informe-avance-02-ecosense.pdf
```

Checklist verbal para cerrar el video:

- 8 de 20 casos de uso implementados: RF10-RF17, equivalente a 40%.
- 83 tests JVM, 0 failures, 0 errors.
- JaCoCo lines >= 70% y branches >= 70%.
- BDD ejecutable con Gherkin + glue.
- Integracion IT-01 a IT-17.
- Sonar historico OK y reanalisis local pendiente solo por Docker Desktop.
