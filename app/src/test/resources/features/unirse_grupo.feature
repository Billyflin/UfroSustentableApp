# language: es
Característica: Unirse a un grupo (CU-10)

  Antecedentes:
    Dado que existen los siguientes grupos disponibles:
      | id   | nombre           | tipo    | puntajeTotal | capacidad |
      | G001 | EcoVerde         | PUBLICO | 100          | 10        |
      | G002 | RecicladoresUFRO | PRIVADO | 50           | 5         |
      | G003 | FullGroup        | PUBLICO | 200          | 2         |

  @positivo
  Escenario: Un usuario se une a un grupo publico
    Dado que el usuario "U001" con email "ana@ufro.cl" no pertenece a ningún grupo y tiene 20 puntos
    Cuando el usuario intenta unirse al grupo "G001"
    Entonces el resultado debe ser Exitoso
    Y el usuario debe pertenecer al grupo "G001"

  @positivo
  Escenario: Puntos se suman al grupo
    Dado que el usuario "U002" con email "carlos@ufro.cl" no pertenece a ningún grupo y tiene 30 puntos
    Cuando el usuario intenta unirse al grupo "G001"
    Entonces el resultado debe ser Exitoso
    Y el puntaje total del grupo "G001" debe ser 130

  @negativo
  Escenario: Usuario ya pertenece a otro grupo
    Dado que el usuario "U004" con email "pedro@ufro.cl" ya pertenece al grupo "G002" y tiene 10 puntos
    Cuando el usuario intenta unirse al grupo "G001"
    Entonces el resultado debe ser un Error
    Y el mensaje de error debe ser "El usuario ya pertenece a un grupo"

  @negativo
  Escenario: Grupo no existe
    Dado que el usuario "U006" con email "jose@ufro.cl" no pertenece a ningún grupo y tiene 0 puntos
    Cuando el usuario intenta unirse al grupo "G-NON-EXISTENT"
    Entonces el resultado debe ser un Error
    Y el mensaje de error debe ser "Grupo no encontrado"

  @frontera
  Escenario: Grupo lleno
    Dado que el grupo "G003" esta lleno
    Y que el usuario "U007" con email "lisa@ufro.cl" no pertenece a ningún grupo y tiene 0 puntos
    Cuando el usuario intenta unirse al grupo "G003"
    Entonces el resultado debe ser un Error
    Y el mensaje de error debe ser "El grupo ha alcanzado su capacidad maxima"
