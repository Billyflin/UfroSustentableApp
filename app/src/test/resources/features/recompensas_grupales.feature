# language: es
Característica: Reclamar recompensas grupales (CU-11)

  Antecedentes:
    Dado que existen los siguientes usuarios:
      | id   | email          | puntos | grupoId |
      | U001 | ana@ufro.cl    | 200    | G001    |
      | U002 | bob@ufro.cl    | 300    | G001    |
      | U003 | carla@ufro.cl  | 100    | null    |
    Y los siguientes grupos con sus puntajes:
      | id   | nombre    | puntajeTotal | recompensasDisponibles |
      | G001 | EcoElite  | 500          | Insignia Oro           |
      | G002 | Vacio     | 0            |                        |

  @positivo
  Escenario: Un miembro del grupo reclama una recompensa disponible
    Cuando el usuario "U001" intenta reclamar la recompensa grupal "Insignia Oro"
    Entonces el resultado debe ser Exitoso
    Y el usuario "U001" debe tener la recompensa "Insignia Oro" en su perfil

  @positivo
  Escenario: Visualizar recompensas disponibles del grupo
    Cuando el usuario "U001" consulta las recompensas disponibles de su grupo
    Entonces debe ver la recompensa "Insignia Oro" en la lista

  @negativo
  Escenario: Un usuario intenta reclamar una recompensa sin pertenecer a un grupo
    Cuando el usuario "U003" intenta reclamar la recompensa grupal "Insignia Oro"
    Entonces el resultado debe ser un Error
    Y el mensaje de error debe ser "El usuario no pertenece a un grupo"

  @negativo
  Escenario: Un miembro intenta reclamar una recompensa que el grupo aun no desbloquea
    Cuando el usuario "U001" intenta reclamar la recompensa grupal "Trofeo Diamante"
    Entonces el resultado debe ser un Error
    Y el mensaje de error debe ser "Recompensa no disponible para el grupo"

  @frontera
  Escenario: Un miembro reclama una recompensa justo cuando se alcanza la meta de puntos
    Dado que el usuario "U002" obtiene 700 puntos nuevos
    Y el grupo "G001" ahora tiene la recompensa "Trofeo Diamante"
    Cuando el usuario "U002" intenta reclamar la recompensa grupal "Trofeo Diamante"
    Entonces el resultado debe ser Exitoso
    Y el usuario "U002" debe tener la recompensa "Trofeo Diamante" en su perfil
