# Guion video Avance 02 EcoSense

Duracion objetivo: 4 a 5 minutos.

1. Mostrar rama y estado:

```powershell
git status --short --branch
```

2. Ejecutar pruebas unitarias, BDD e integracion con cobertura:

```powershell
.\gradlew.bat :app:jacocoDebugUnitTestReport --rerun-tasks --console=plain
```

3. Abrir reportes locales:

```powershell
start app\build\reports\tests\testDebugUnitTest\index.html
start app\build\reports\cucumber\cucumber-report.html
start app\build\reports\jacoco\jacocoDebugUnitTestReport\html\index.html
```

4. Mostrar evidencia Sonar previa y explicar que el dashboard se regenera con Docker Desktop activo:

```powershell
Get-Content docs\informe-sonarqube-ecosense.md
Get-Content docs\sonarqube-qualitygate.json
Get-Content docs\sonarqube-metrics.json
Get-Content docs\sonarqube-resolved-issues.json
powershell -ExecutionPolicy Bypass -File .\scripts\run-sonarqube-analysis.ps1
```

5. Mostrar evidencia historica complementaria:

```powershell
Get-Content docs\pruebas-integracion-ecosense.md
Get-Content docs\diagramas-c4\README.md
```

6. Mostrar el informe final:

```powershell
start docs\informe-avance-02-ecosense.pdf
```
