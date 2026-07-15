# Guion de presentacion final EcoSense

Duracion objetivo: 8 a 10 minutos.

## Tesis de la defensa

EcoSense cumple el 100% del alcance funcional priorizado y lo demuestra con evidencia reproducible: TDD, BDD, integracion, cobertura, SonarQube y rendimiento Android medido antes y despues.

Por acuerdo con el docente, E2E y carga/Grafana se sustituyeron dentro del alcance evaluado por optimizacion y benchmarks Android. No presentarlo como trabajo faltante.

## Recorrido por laminas

| Lamina | Tiempo | Mensaje oral |
|---|---:|---|
| 1 | 25 s | EcoSense no solo esta implementado: su calidad y rendimiento se pueden demostrar con evidencia real. |
| 2 | 35 s | Abrir con los cuatro resultados: 8/8 casos, 83 tests, 89,23% de lineas y APK 75,77% mas pequeno. |
| 3 | 35 s | Mostrar el producto funcionando: historial de solicitudes, grupos con nombres legibles y perfil limpio. |
| 4 | 45 s | Recorrer RF10-RF17. Los ocho casos priorizados estan implementados, con 12 escenarios Gherkin y 17 flujos de integracion. |
| 5 | 40 s | Explicar la separacion entre Compose, ViewModels, servicios y dominio. Firebase queda detras de puertos que permiten usar fakes en pruebas. |
| 6 | 55 s | Mostrar el ciclo red-green-refactor y los commits. Leer brevemente RF17 y explicar que el glue llama al servicio de dominio real. |
| 7 | 55 s | Defender cobertura: 89,23% lineas y 77,88% ramas, ambas sobre 70%. Los 83 tests pasan sin fallos y la integracion cubre IT-01 a IT-17. |
| 8 | 45 s | Declarar el acuerdo docente: E2E web y carga/Grafana se reemplazaron por startup, frame timing, APK y Baseline Profile porque EcoSense es un cliente Android sobre Firebase sin API backend propia. |
| 9 | 50 s | Presentar la historia completa de Sonar: 22 smells detectados, 2 corregidos y 20 abiertos en la medicion historica; 0 bugs, 0 vulnerabilidades, 0% duplicacion y ratings A. |
| 10 | 55 s | Mostrar el antes/despues: APK 33,12 a 8,02 MiB y startup mediana 5.596 a 3.842 ms. Macrobenchmark queda como linea base reproducible. |
| 11 | 50 s | Contar la validacion en el S22: QR de Rectoria, consulta Firestore, cierre de camara, solicitud de 3 kg, nombres de miembros, refresco del historial y nuevo mapa. |
| 12 | 35 s | Cerrar con los cinco hechos y, si hay tiempo, ejecutar el recorrido Mapa -> QR -> Historial. |
| 13 | respaldo | Usar solo ante preguntas: contiene comandos y rutas exactas de informes, BDD, integracion, Sonar, performance y QR. |

## Respuesta ante E2E y Grafana

> EcoSense es una aplicacion Android cliente sobre Firebase y no expone una API backend propia para aplicar un perfil de usuarios virtuales representativo. Por acuerdo con el docente, sustituimos E2E y carga/Grafana por optimizacion y benchmarks Android reproducibles: startup cold, warm y hot, frame timing de navegacion, tamano del APK y comparacion antes/despues.

## Respuesta ante cobertura Sonar 0%

> El 0% pertenece a la ejecucion historica de Sonar que no importo el XML JaCoCo. La cobertura actual se midio localmente con JaCoCo: 89,23% de lineas y 77,88% de ramas. La presentacion separa ambas fuentes para no mezclar resultados.

## Respuesta ante Firebase Storage

> La solicitud sigue siendo consistente si el bucket de imagenes no esta aprovisionado: se persiste en Firestore sin foto y se informa al usuario. La prueba real de 3 kg de plastico en Rectoria quedo registrada sin caida ni escritura parcial.

## Frase de cierre

> La calidad de EcoSense no se declara: se ejecuta, se mide y se puede repetir.
