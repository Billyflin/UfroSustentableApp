# QR de prueba EcoSense

Estos codigos permiten probar el flujo real `Camara -> ML Kit -> Firestore -> Formulario de reciclaje`.

El contenido de cada QR es el ID exacto de un documento de la coleccion `recycling_points` del proyecto Firebase `ecosense-40628`. No contiene JSON.

| Archivo | Contenido QR | Documento Firestore | Resultado esperado |
|---|---|---|---|
| [qr-ufro-rectoria.png](qr-ufro-rectoria.png) | `ufro-rectoria` | Rectoría UFRO | Abre el formulario mostrando Rectoría UFRO. |
| [qr-ufro-biblioteca.png](qr-ufro-biblioteca.png) | `ufro-biblioteca` | Biblioteca Central UFRO | Abre el formulario mostrando Biblioteca Central UFRO. |
| [qr-ufro-ingenieria.png](qr-ufro-ingenieria.png) | `ufro-ingenieria` | Facultad de Ingeniería y Ciencias | Abre el formulario mostrando la facultad. |
| [qr-ufro-casino-norte.png](qr-ufro-casino-norte.png) | `ufro-casino-norte` | Casino Norte UFRO | Abre el formulario mostrando Casino Norte UFRO. |
| [qr-invalido-prueba.png](qr-invalido-prueba.png) | `punto-inexistente-prueba` | No existe | Permanece en el escáner y muestra `Código QR no reconocido`. |

La hoja [qr-pruebas-ecosense.png](qr-pruebas-ecosense.png) contiene todos los códigos en una sola imagen para mostrar o imprimir.

## Procedimiento

1. Abrir uno de los PNG en el monitor con zoom de 100% o superior.
2. En EcoSense, pulsar el botón central `Escanear QR`.
3. Apuntar la cámara al código y mantener el teléfono estable.
4. Para un QR válido, comprobar que se abre el formulario y aparece el nombre del punto.
5. Para el QR inválido, comprobar que se muestra el mensaje de rechazo y que la cámara continúa activa.

## Datos registrados en Firestore

| ID | Latitud | Longitud | Descripción |
|---|---:|---:|---|
| `ufro-rectoria` | -38.74621 | -72.61554 | Rectoría UFRO |
| `ufro-biblioteca` | -38.74553 | -72.61482 | Biblioteca Central UFRO |
| `ufro-ingenieria` | -38.74801 | -72.61627 | Facultad de Ingeniería y Ciencias |
| `ufro-casino-norte` | -38.74468 | -72.61594 | Casino Norte UFRO |
