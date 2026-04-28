# language: es
Característica: Gestionar y participar en grupos

  Antecedentes:
    Dado un sistema con los siguientes usuarios base:
      | id   | email          | puntos | grupoId |
      | U001 | juan@ufro.cl   | 100    | G001    |
      | U002 | pedro@ufro.cl  | 200    | G001    |
      | U003 | maria@ufro.cl  | 0      | null    |
    Y los siguientes grupos base:
      | id   | nombre     | tipo    | puntajeTotal | miembros  | admins |
      | G001 | Los Verdes | PUBLICO | 300          | U001,U002 | U001   |
      | G002 | Top Group  | PUBLICO | 800          |           |        |

  Escenario: Crear un grupo nuevo exitosamente
    Cuando el usuario "U003" crea un grupo "Recicladores" de tipo "PUBLICO"
    Entonces la accion debe ser exitosa
    Y el usuario "U003" debe ser administrador de su grupo

  Escenario: Sumar puntos al grupo y desbloquear recompensas
    Cuando el usuario "U002" obtiene 200 puntos nuevos
    Entonces el grupo "G001" debe tener 500 puntos en total
    Y el grupo "G001" debe tener la recompensa "Insignia Oro"

  Escenario: Administrador elimina a un miembro del grupo
    Cuando el administrador "U001" elimina al miembro "U002" del grupo "G001"
    Entonces la accion debe ser exitosa
    Y el usuario "U002" ya no pertenece al grupo "G001"
    Y el grupo "G001" debe tener 100 puntos en total

  Escenario: Visualizar rankings de grupos
    Cuando consulto el ranking global de grupos
    Entonces el grupo "G002" debe estar en la posicion 1
    Y el grupo "G001" debe estar en la posicion 2

  Escenario: Visualizar ranking interno de grupo
    Cuando consulto el ranking interno del grupo "G001"
    Entonces el usuario "U002" debe estar en la posicion 1
    Y el usuario "U001" debe estar en la posicion 2

  Escenario: Administrador agrega a un nuevo miembro
    Cuando el administrador "U001" agrega al usuario "U003" al grupo "G001"
    Entonces la accion debe ser exitosa
    Y el usuario debe pertenecer al grupo "G001"

  Escenario: Administrador promueve a un miembro a administrador
    Cuando el administrador "U001" promueve al miembro "U002" a administrador del grupo "G001"
    Entonces la accion debe ser exitosa
    Y el usuario "U002" debe ser administrador de su grupo
