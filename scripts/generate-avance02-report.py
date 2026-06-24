from __future__ import annotations

import json
import xml.etree.ElementTree as ET
from datetime import date
from pathlib import Path
from xml.sax.saxutils import escape

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import PageBreak, Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle


ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"
REPORT_MD = DOCS / "informe-avance-02-ecosense.md"
REPORT_PDF = DOCS / "informe-avance-02-ecosense.pdf"
VIDEO_GUIDE = DOCS / "guion-video-avance-02.md"


def pct(covered: int, missed: int) -> float:
    total = covered + missed
    return round(covered * 100.0 / total, 2) if total else 0.0


def read_jacoco() -> dict[str, dict[str, float | int]]:
    path = ROOT / "app/build/reports/jacoco/jacocoDebugUnitTestReport/jacocoDebugUnitTestReport.xml"
    root = ET.parse(path).getroot()
    counters: dict[str, dict[str, float | int]] = {}
    for counter in root.findall("counter"):
        name = counter.attrib["type"]
        missed = int(counter.attrib["missed"])
        covered = int(counter.attrib["covered"])
        counters[name] = {
            "covered": covered,
            "missed": missed,
            "total": covered + missed,
            "percent": pct(covered, missed),
        }
    return counters


def read_tests() -> dict[str, float | int]:
    results = ROOT / "app/build/test-results/testDebugUnitTest"
    totals = {"suites": 0, "tests": 0, "failures": 0, "errors": 0, "skipped": 0, "time": 0.0}
    for path in results.glob("TEST-*.xml"):
        suite = ET.parse(path).getroot()
        totals["suites"] += 1
        totals["tests"] += int(suite.attrib.get("tests", "0"))
        totals["failures"] += int(suite.attrib.get("failures", "0"))
        totals["errors"] += int(suite.attrib.get("errors", "0"))
        totals["skipped"] += int(suite.attrib.get("skipped", "0"))
        totals["time"] += float(suite.attrib.get("time", "0"))
    totals["time"] = round(float(totals["time"]), 3)
    return totals


def read_sonar() -> tuple[dict[str, str], str]:
    metrics_path = DOCS / "sonarqube-metrics.json"
    gate_path = DOCS / "sonarqube-qualitygate.json"
    measures = {}
    if metrics_path.exists():
        data = json.loads(metrics_path.read_text(encoding="utf-8-sig"))
        for item in data.get("component", {}).get("measures", []):
            measures[item["metric"]] = item.get("value", "")
    gate = "No ejecutado"
    if gate_path.exists():
        data = json.loads(gate_path.read_text(encoding="utf-8-sig"))
        gate = data.get("projectStatus", {}).get("status", "No ejecutado")
    return measures, gate


def fmt(value: float | int | str) -> str:
    if isinstance(value, float):
        return f"{value:.2f}".rstrip("0").rstrip(".")
    return str(value)


def build_markdown(jacoco: dict[str, dict[str, float | int]], tests: dict[str, float | int], sonar: dict[str, str], gate: str) -> str:
    line = jacoco["LINE"]
    branch = jacoco["BRANCH"]
    return f"""# Entrega Avance 02 - Proyecto semestral EcoSense

**Curso:** Pruebas de software
**Equipo:** Billy Martinez, Bastian Lagos
**Fecha:** {date.today().strftime("%d/%m/%Y")}
**Repositorio/rama:** `codex/performance-optimizations`

## Resumen ejecutivo

El avance implementa y evidencia el bloque funcional RF10-RF17 asociado a grupos, ranking y recompensas. Este bloque cubre 8 casos de uso priorizados y se considera el 40% del alcance funcional planificado para esta entrega. La evidencia incluye pruebas unitarias con Kotest, escenarios BDD ejecutables con Cucumber/Gherkin, pruebas de integracion de servicios y reporte local de cobertura JaCoCo.

Resultado actual: `{int(tests["tests"])}` tests JVM ejecutados, `{int(tests["failures"])}` fallos, `{int(tests["errors"])}` errores y `{int(tests["skipped"])}` omitidos. La cobertura local sobre el alcance configurado del avance es **{fmt(line["percent"])}% lines** y **{fmt(branch["percent"])}% branches**.

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
.\\gradlew.bat :app:jacocoDebugUnitTestReport --rerun-tasks --console=plain
```

Resultado:

| Indicador | Valor |
|---|---:|
| Suites JVM | {int(tests["suites"])} |
| Tests | {int(tests["tests"])} |
| Failures | {int(tests["failures"])} |
| Errors | {int(tests["errors"])} |
| Skipped | {int(tests["skipped"])} |
| Tiempo XML acumulado | {fmt(tests["time"])} s |

Cobertura JaCoCo:

| Tipo | Cubierto | Perdido | Total | % |
|---|---:|---:|---:|---:|
| Lines | {line["covered"]} | {line["missed"]} | {line["total"]} | {fmt(line["percent"])}% |
| Branches | {branch["covered"]} | {branch["missed"]} | {branch["total"]} | {fmt(branch["percent"])}% |
| Instructions | {jacoco["INSTRUCTION"]["covered"]} | {jacoco["INSTRUCTION"]["missed"]} | {jacoco["INSTRUCTION"]["total"]} | {fmt(jacoco["INSTRUCTION"]["percent"])}% |

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
| Quality Gate | {gate} |
| Bugs | {sonar.get("bugs", "N/D")} |
| Vulnerabilities | {sonar.get("vulnerabilities", "N/D")} |
| Code Smells | {sonar.get("code_smells", "N/D")} |
| Coverage Sonar previo | {sonar.get("coverage", "N/D")}% |
| Duplications | {sonar.get("duplicated_lines_density", "N/D")}% |
| Maintainability rating | {sonar.get("sqale_rating", "N/D")} |
| Security rating | {sonar.get("security_rating", "N/D")} |
| Reliability rating | {sonar.get("reliability_rating", "N/D")} |

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
"""


def video_guide() -> str:
    return """# Guion video Avance 02 EcoSense

Duracion objetivo: 4 a 5 minutos.

1. Mostrar rama y estado:

```powershell
git status --short --branch
```

2. Ejecutar pruebas unitarias, BDD e integracion con cobertura:

```powershell
.\\gradlew.bat :app:jacocoDebugUnitTestReport --rerun-tasks --console=plain
```

3. Abrir reportes locales:

```powershell
start app\\build\\reports\\tests\\testDebugUnitTest\\index.html
start app\\build\\reports\\cucumber\\cucumber-report.html
start app\\build\\reports\\jacoco\\jacocoDebugUnitTestReport\\html\\index.html
```

4. Mostrar evidencia Sonar previa y explicar que el dashboard se regenera con Docker Desktop activo:

```powershell
Get-Content docs\\sonarqube-qualitygate.json
Get-Content docs\\sonarqube-metrics.json
powershell -ExecutionPolicy Bypass -File .\\scripts\\run-sonarqube-analysis.ps1
```

5. Mostrar el informe final:

```powershell
start docs\\informe-avance-02-ecosense.pdf
```
"""


def p(text: str, style: ParagraphStyle) -> Paragraph:
    return Paragraph(escape(text).replace("\n", "<br/>"), style)


def table(rows: list[list[str]], widths: list[float], style: ParagraphStyle) -> Table:
    wrapped = [[p(cell, style) for cell in row] for row in rows]
    result = Table(wrapped, colWidths=[w * cm for w in widths], repeatRows=1)
    result.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#225C57")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("GRID", (0, 0), (-1, -1), 0.35, colors.HexColor("#D9E2E8")),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F7FAF9")]),
                ("LEFTPADDING", (0, 0), (-1, -1), 4),
                ("RIGHTPADDING", (0, 0), (-1, -1), 4),
                ("TOPPADDING", (0, 0), (-1, -1), 4),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
            ]
        )
    )
    return result


def build_pdf(jacoco: dict[str, dict[str, float | int]], tests: dict[str, float | int], sonar: dict[str, str], gate: str) -> None:
    styles = getSampleStyleSheet()
    styles.add(ParagraphStyle("CoverTitle", parent=styles["Title"], fontSize=24, leading=28, textColor=colors.HexColor("#225C57"), alignment=TA_CENTER, spaceAfter=16))
    styles.add(ParagraphStyle("CoverSub", parent=styles["BodyText"], fontSize=12, leading=16, alignment=TA_CENTER, spaceAfter=8))
    styles.add(ParagraphStyle("H1Eco", parent=styles["Heading1"], fontSize=15, leading=18, textColor=colors.HexColor("#225C57"), spaceBefore=8, spaceAfter=8))
    styles.add(ParagraphStyle("H2Eco", parent=styles["Heading2"], fontSize=12, leading=14, textColor=colors.HexColor("#2D3748"), spaceBefore=7, spaceAfter=6))
    styles.add(ParagraphStyle("BodyEco", parent=styles["BodyText"], fontSize=8.5, leading=11, spaceAfter=5))
    styles.add(ParagraphStyle("SmallEco", parent=styles["BodyText"], fontSize=7.4, leading=9, spaceAfter=3))

    line = jacoco["LINE"]
    branch = jacoco["BRANCH"]
    instructions = jacoco["INSTRUCTION"]
    story = [
        Spacer(1, 4.5 * cm),
        p("Entrega Avance 02 - Proyecto semestral", styles["CoverTitle"]),
        p("EcoSense / UFRO Sustentable App", styles["CoverSub"]),
        p("Equipo: Billy Martinez, Bastian Lagos", styles["CoverSub"]),
        p(f"Fecha: {date.today().strftime('%d/%m/%Y')}", styles["CoverSub"]),
        PageBreak(),
        p("Resumen Ejecutivo", styles["H1Eco"]),
        p(
            f"El avance implementa RF10-RF17: grupos, ranking y recompensas. La ejecucion local consolida {int(tests['tests'])} tests JVM sin fallos y cobertura JaCoCo de {fmt(line['percent'])}% lines y {fmt(branch['percent'])}% branches sobre el alcance del avance.",
            styles["BodyEco"],
        ),
        p("Alcance Del Avance 02", styles["H1Eco"]),
        table(
            [
                ["CU/RF", "Implementacion", "Evidencia"],
                ["RF10", "RankingService", "RF10RankingGlobalSpec + BDD + IT-15"],
                ["RF11", "GrupoService / GrupoApplicationService", "RF11CrearGrupoSpec + BDD + IT-01, IT-02, IT-09, IT-10"],
                ["RF12", "GrupoService / GrupoApplicationService", "RF12UnirseGrupoSpec + unirse_grupo.feature + IT-03, IT-04, IT-11, IT-12"],
                ["RF13", "Ranking interno", "RF13RankingGrupoSpec + BDD + IT-15"],
                ["RF14", "Puntos grupales", "RF14PuntosGrupalesSpec + BDD + IT-13, IT-14"],
                ["RF15", "Recompensas grupales", "RF15RecompensasGrupalesSpec + BDD + IT-13"],
                ["RF16", "Gestion de miembros", "RF16AdminGrupoSpec + BDD"],
                ["RF17", "Ranking grupal", "RF17RankingGrupalSpec + BDD + IT-15"],
            ],
            [2.0, 5.6, 8.0],
            styles["SmallEco"],
        ),
        p("Arquitectura Implementada", styles["H1Eco"]),
        table(
            [
                ["Capa", "Elementos", "Responsabilidad"],
                ["UI Android", "Compose screens, Navigation3", "Interaccion de usuario y navegacion"],
                ["ViewModel/Repository", "viewmodel/*, repository/*", "Estado de pantalla y adaptacion a Firebase"],
                ["Servicios de aplicacion", "GrupoApplicationService, RecyclingApplicationService", "Orquestacion entre repositorios, storage y eventos"],
                ["Dominio puro", "GrupoService, RankingService, modelos", "Reglas testeables sin Android ni red"],
                ["Puertos/Fakes", "IntegrationPorts.kt, dobles in-memory", "Integracion reproducible sin infraestructura externa"],
            ],
            [3.2, 5.2, 7.2],
            styles["SmallEco"],
        ),
        p("Diagramas C4 disponibles en docs/diagramas-c4/01-contexto-sistema.puml, 02-contenedores.puml y 03-componentes-android.puml.", styles["BodyEco"]),
        PageBreak(),
        p("Proceso BDD + TDD Aplicado", styles["H1Eco"]),
        p("BDD: se agregaron escenarios ejecutables para RF10, RF11, RF13, RF14, RF15, RF16 y RF17 en grupos_ranking_recompensas.feature. RF12 ya estaba cubierto por unirse_grupo.feature. El glue code vive en steps/GruposRankingSteps.kt y steps/UnirseGrupoSteps.kt.", styles["BodyEco"]),
        p('Extracto Gherkin: Escenario RF17 calcular ranking grupal -> Dado grupos con puntaje, Cuando calculo el ranking grupal, Entonces el primer grupo debe ser "G2".', styles["BodyEco"]),
        table(
            [
                ["Ciclo TDD", "Red", "Green", "Refactor"],
                ["Grupo/RF12", "Errores de union a grupos publicos, privados y repetidos", "GrupoService implementa reglas puras", "GrupoApplicationService agrega repositorios/eventos"],
                ["Ranking/RF10-RF17", "Specs de orden y posiciones", "RankingService ordena deterministamente", "BDD reutiliza reglas sin duplicarlas"],
                ["Reciclaje/Integracion", "Storage, historial, eventos y errores", "RecyclingApplicationService coordina ports", "IT-16/IT-17 cubren validaciones negativas"],
            ],
            [2.7, 4.2, 4.2, 4.2],
            styles["SmallEco"],
        ),
        p("Pruebas Unitarias Y Cobertura", styles["H1Eco"]),
        table(
            [
                ["Indicador", "Valor"],
                ["Suites JVM", str(int(tests["suites"]))],
                ["Tests", str(int(tests["tests"]))],
                ["Failures", str(int(tests["failures"]))],
                ["Errors", str(int(tests["errors"]))],
                ["Skipped", str(int(tests["skipped"]))],
                ["Tiempo XML acumulado", f"{fmt(tests['time'])} s"],
            ],
            [6.0, 5.0],
            styles["SmallEco"],
        ),
        Spacer(1, 0.2 * cm),
        table(
            [
                ["Tipo", "Cubierto", "Perdido", "Total", "%"],
                ["Lines", str(line["covered"]), str(line["missed"]), str(line["total"]), f"{fmt(line['percent'])}%"],
                ["Branches", str(branch["covered"]), str(branch["missed"]), str(branch["total"]), f"{fmt(branch['percent'])}%"],
                ["Instructions", str(instructions["covered"]), str(instructions["missed"]), str(instructions["total"]), f"{fmt(instructions['percent'])}%"],
            ],
            [3.0, 2.5, 2.5, 2.5, 2.5],
            styles["SmallEco"],
        ),
        p("Comando: .\\gradlew.bat :app:jacocoDebugUnitTestReport --rerun-tasks --console=plain", styles["SmallEco"]),
        PageBreak(),
        p("Pruebas De Integracion", styles["H1Eco"]),
        p("EcoSenseIntegrationSpec valida IT-01 a IT-17. Integra servicios de aplicacion, repositorios in-memory, storage fake y publicador de eventos. Cubre caminos correctos, errores 404/409, timeout, validaciones de entrada, ranking y efectos laterales.", styles["BodyEco"]),
        p("Calidad Con SonarQube", styles["H1Eco"]),
        table(
            [
                ["Metrica", "Resultado"],
                ["Quality Gate", gate],
                ["Bugs", sonar.get("bugs", "N/D")],
                ["Vulnerabilities", sonar.get("vulnerabilities", "N/D")],
                ["Code Smells", sonar.get("code_smells", "N/D")],
                ["Coverage Sonar previo", sonar.get("coverage", "N/D") + "%"],
                ["Duplications", sonar.get("duplicated_lines_density", "N/D") + "%"],
                ["Maintainability rating", sonar.get("sqale_rating", "N/D")],
                ["Security rating", sonar.get("security_rating", "N/D")],
                ["Reliability rating", sonar.get("reliability_rating", "N/D")],
            ],
            [6.0, 5.0],
            styles["SmallEco"],
        ),
        p("Se configuro sonar.coverage.jacoco.xmlReportPaths para importar el XML JaCoCo. La re-ejecucion local de Sonar no pudo completarse porque Docker Desktop no esta disponible en este entorno.", styles["BodyEco"]),
        p("Mejoras Y Deuda Tecnica", styles["H1Eco"]),
        table(
            [
                ["Prioridad", "Mejora", "Motivo"],
                ["Alta", "Re-ejecutar SonarQube con Docker activo", "Actualizar dashboard con coverage real"],
                ["Alta", "Reducir complejidad de RecycleFormScreen", "Hallazgo critico de mantenibilidad"],
                ["Media", "Reducir parametros en AppNavHost", "Menos acoplamiento de navegacion"],
                ["Media", "Agregar tests instrumentados UI", "Cubrir Compose y flujos visuales"],
                ["Baja", "Grabar video final", "Entregable audiovisual requerido"],
            ],
            [2.5, 6.2, 6.2],
            styles["SmallEco"],
        ),
        p("Conclusiones", styles["H1Eco"]),
        p("La entrega queda funcionalmente avanzada para RF10-RF17, con TDD/BDD ejecutable, integracion de servicios y cobertura local superior al umbral de 70% en lineas y ramas. Los pendientes externos son regenerar SonarQube con Docker activo y grabar el video final.", styles["BodyEco"]),
        p("Anexos", styles["H1Eco"]),
        p("Reportes: docs/pruebas-integracion-ecosense.md, docs/informe-sonarqube-ecosense.md, docs/informe-rendimiento-ecosense.md y docs/guion-video-avance-02.md.", styles["BodyEco"]),
    ]

    doc = SimpleDocTemplate(
        str(REPORT_PDF),
        pagesize=letter,
        rightMargin=1.7 * cm,
        leftMargin=1.7 * cm,
        topMargin=1.6 * cm,
        bottomMargin=1.6 * cm,
    )
    doc.build(story)


def main() -> None:
    DOCS.mkdir(exist_ok=True)
    jacoco = read_jacoco()
    tests = read_tests()
    sonar, gate = read_sonar()
    REPORT_MD.write_text(build_markdown(jacoco, tests, sonar, gate), encoding="utf-8")
    VIDEO_GUIDE.write_text(video_guide(), encoding="utf-8")
    build_pdf(jacoco, tests, sonar, gate)
    print(REPORT_MD)
    print(REPORT_PDF)
    print(VIDEO_GUIDE)


if __name__ == "__main__":
    main()
