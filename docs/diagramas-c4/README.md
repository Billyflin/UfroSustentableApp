# Diagramas C4 - EcoSense

Diagramas de arquitectura del proyecto EcoSense en PlantUML usando C4-PlantUML.

## Archivos

- `01-contexto-sistema.puml`: nivel 1, contexto del sistema.
- `02-contenedores.puml`: nivel 2, contenedores principales y servicios externos.
- `03-componentes-android.puml`: nivel 3, componentes internos de la app Android.

## Renderizado

Opcion con PlantUML local:

```powershell
java -jar plantuml.jar docs/diagramas-c4/*.puml
```

Opcion con Docker:

```powershell
docker run --rm -v ${PWD}:/workspace plantuml/plantuml -tpng /workspace/docs/diagramas-c4/*.puml
```

Los archivos usan `!includeurl` para cargar C4-PlantUML desde GitHub. Para renderizar sin internet, descargar C4-PlantUML y reemplazar los `!includeurl` por rutas locales.
