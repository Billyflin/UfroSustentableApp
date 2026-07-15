# language: es
Característica: Grupos, ranking y recompensas del avance 02
  Como estudiante de EcoSense
  Quiero ver rankings, crear grupos y recibir recompensas grupales
  Para competir y colaborar en acciones sustentables

  Escenario: RF10 calcular ranking global de usuarios
    Dado los siguientes usuarios para ranking global:
      | id | email         | nombre | puntos |
      | U1 | ana@ufro.cl   | Ana    | 200    |
      | U2 | bob@ufro.cl   | Bob    | 50     |
      | U3 | carlos@ufro.cl| Carlos | 150    |
    Cuando calculo el ranking global de usuarios
    Entonces el primer usuario del ranking global debe ser "U1"
    Y la posicion global del usuario "U3" debe ser 2

  Escenario: RF11 crear un grupo publico
    Dado un servicio de grupos nuevo para BDD
    Cuando el usuario "U001" crea un grupo publico llamado "EcoVerde"
    Entonces el grupo se crea exitosamente con nombre "EcoVerde"
    Y el usuario "U001" queda como administrador del grupo creado

  Escenario: RF13 calcular ranking interno de un grupo
    Dado el grupo "G001" con miembros para ranking interno:
      | usuarioId |
      | U1        |
      | U2        |
      | U3        |
    Y los siguientes usuarios para ranking interno:
      | id | email          | nombre | puntos |
      | U1 | ana@ufro.cl    | Ana    | 300    |
      | U2 | bob@ufro.cl    | Bob    | 100    |
      | U3 | carlos@ufro.cl | Carlos | 200    |
      | U4 | diana@ufro.cl  | Diana  | 250    |
    Cuando calculo el ranking interno del grupo
    Entonces el primer miembro del ranking interno debe ser "U1"
    Y la posicion global interna del usuario "U3" debe ser 3

  Escenario: RF14 sumar puntos personales al grupo del usuario
    Dado un servicio de grupos nuevo para BDD
    Y existe el grupo publico "G001" con puntaje 100
    Y el usuario "U010" pertenece al grupo "G001" con 50 puntos
    Cuando agrego 30 puntos sustentables al usuario
    Entonces el usuario queda con 80 puntos
    Y el grupo "G001" queda con 130 puntos

  Escenario: RF15 habilitar recompensa grupal al cumplir la meta
    Dado un servicio de grupos nuevo para BDD
    Y existe el grupo publico "G777" con puntaje 1200 y meta 1000
    Cuando verifico recompensa grupal para "G777"
    Entonces la recompensa grupal debe estar disponible para "G777"

  Escenario: RF16 administrar miembros de un grupo
    Dado un servicio de grupos nuevo para BDD
    Y existe el grupo "G900" con administrador "ADMIN" y miembro "U001"
    Cuando el administrador "ADMIN" agrega al usuario "U002" en el grupo "G900"
    Entonces la gestion de miembros debe ser exitosa
    Y el usuario "U002" debe aparecer como miembro del grupo "G900"

  Escenario: RF17 calcular ranking grupal
    Dado los siguientes grupos para ranking grupal:
      | id | nombre           | puntajeTotal |
      | G1 | EcoVerde         | 500          |
      | G2 | RecicladoresUFRO | 800          |
      | G3 | GreenTeam        | 200          |
    Cuando calculo el ranking grupal
    Entonces el primer grupo del ranking grupal debe ser "G2"
    Y la posicion del grupo "G1" debe ser 2
