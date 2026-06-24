# Guion presentacion final EcoSense

Duracion sugerida: 8 a 10 minutos.

## Enfoque

Esta presentacion no debe vender como completado lo que aun no esta evidenciado en el repositorio. La narrativa recomendada es:

1. EcoSense ya tiene una base tecnica fuerte y medible.
2. El bloque RF10-RF17 esta implementado y probado con TDD/BDD/integracion.
3. La pauta final exige 100% de casos de uso, E2E, carga/estres con Grafana y Sonar actualizado.
4. Esas brechas deben cerrarse antes de defender la entrega como 100% final.

## Recorrido por laminas

| Lamina | Mensaje oral |
|---|---|
| 1 | Presentar el proyecto y anticipar que la evidencia actual es parcial frente a la pauta final. |
| 2 | Resumir los resultados duros: 83 tests, 89.23% lines, 77.88% branches y mejoras de rendimiento. |
| 3 | Explicar la diferencia entre 20 CU planificados, 8 evidenciados y 100% requerido por la pauta final. |
| 4 | Mostrar RF10-RF17 como bloque funcional real: grupos, ranking y recompensas. |
| 5 | Explicar arquitectura por capas: UI, ViewModel/Repository, servicios de aplicacion, dominio y puertos. |
| 6 | Conectar casos de uso con servicios y tipos de evidencia. |
| 7 | Presentar la estrategia de calidad: unitarias, BDD, integracion, Sonar y performance. |
| 8 | Explicar TDD + BDD con ejemplos de RankingService y GrupoService. |
| 9 | Leer brevemente el ejemplo Gherkin y explicar que todos los escenarios definidos pasan. |
| 10 | Mostrar cobertura JaCoCo y comando reproducible. |
| 11 | Mostrar integracion IT-01 a IT-17 y explicar uso de dobles in-memory. |
| 12 | Reconocer brecha E2E y proponer Appium/Maestro por cada CU. |
| 13 | Reconocer brecha de carga/estres con Grafana y separar esto de Macrobenchmark mobile. |
| 14 | Mostrar Sonar historico: Quality Gate OK, 0 bugs, 0 vulnerabilities, 20 smells. |
| 15 | Mostrar mejoras de performance mobile: APK y startup. |
| 16 | Cerrar con roadmap concreto para cumplir la pauta final. |
| 17 | Concluir: base solida, falta evidencia de cierre. |
| 18 | Usar como respaldo para rutas del repo y preguntas. |

## Frase de cierre recomendada

> El proyecto tiene una base de calidad real: pruebas, cobertura, BDD, integracion y rendimiento medido. El punto critico para la entrega final no es empezar de cero, sino cerrar la evidencia faltante: 100% de CU, E2E, carga con Grafana y Sonar actualizado.
