# language: es
Característica: Unirse a un grupo (CU-10)
  Como usuario de UfroSustentable
  Quiero unirme a un grupo
  Para colaborar con otros usuarios en actividades sustentables

  Antecedentes:
    Dado que existen los siguientes grupos disponibles:
      | id   | nombre           | tipo    | puntajeTotal |
      | G001 | EcoVerde         | PUBLICO | 100          |
      | G002 | RecicladoresUFRO | PRIVADO | 50           |

  Escenario: Un usuario sin grupo se une a un grupo público exitosamente
    Dado que el usuario "U001" con email "ana@ufro.cl" no pertenece a ningún grupo y tiene 20 puntos
    Cuando el usuario intenta unirse al grupo "G001"
    Entonces el resultado debe ser Exitoso
    Y el usuario debe pertenecer al grupo "G001"

  Escenario: Los puntos del usuario se suman al puntaje total del grupo al unirse
    Dado que el usuario "U002" con email "carlos@ufro.cl" no pertenece a ningún grupo y tiene 30 puntos
    Cuando el usuario intenta unirse al grupo "G001"
    Entonces el resultado debe ser Exitoso
    Y el puntaje total del grupo "G001" debe ser 130

  Escenario: Un usuario sin grupo solicita unirse a un grupo privado y queda pendiente
    Dado que el usuario "U003" con email "lucia@ufro.cl" no pertenece a ningún grupo y tiene 15 puntos
    Cuando el usuario intenta unirse al grupo "G002"
    Entonces el resultado debe ser Pendiente
    Y el usuario no debe pertenecer a ningún grupo

  Escenario: Un usuario que ya pertenece a un grupo intenta unirse a otro grupo
    Dado que el usuario "U004" con email "pedro@ufro.cl" ya pertenece al grupo "G002" y tiene 10 puntos
    Cuando el usuario intenta unirse al grupo "G001"
    Entonces el resultado debe ser un Error
    Y el mensaje de error debe ser "El usuario ya pertenece a un grupo"

  Escenario: Un usuario intenta unirse al mismo grupo al que ya pertenece
    Dado que el usuario "U005" con email "maria@ufro.cl" ya pertenece al grupo "G001" y tiene 25 puntos
    Cuando el usuario intenta unirse al grupo "G001"
    Entonces el resultado debe ser un Error
    Y el mensaje de error debe ser "El usuario ya pertenece a este grupo"
