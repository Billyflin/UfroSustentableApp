3.2.10 Requisito funcional 10
Nombre del Requerimiento: EcoSense App debe calcular y mostrar un ranking global de
usuarios basado en los puntos acumulados dentro de la
aplicación.
Justificación del
Requerimiento:
Fomentar la competitividad entre los usuarios y aumentar la
motivación para participar en actividades de reciclaje.
Secuencia de Operaciones: 1. El usuario accede a la sección de ranking dentro de la
aplicación.
2. El sistema obtiene los puntajes de todos los usuarios.
3. El sistema ordena a los usuarios según sus puntos
acumulados.
4. El sistema muestra los primeros puestos del ranking y la
posición del usuario.
Entradas Necesarias: El usuario inicia sesión en la aplicación.
Accede a la sección de ranking.
Salidas Generadas: Visualización del ranking global y posición del usuario.
Descripción de requisitos del software
Especificación de requisitos de software para la
aplicación Eco Sense App
Rev. 3.0
Pág. 27
Relaciones entre Entradas y
Salidas:
Los puntos acumulados por los usuarios (entrada)
determinan su posición en el ranking (salida).
Respuestas a Excepciones: Si no se pueden cargar los datos del ranking, el sistema
notificará al usuario y sugerirá intentarlo nuevamente.
3.2.11 Requisito funcional 11
Nombre del Requerimiento: EcoSense App debe permitir a los usuarios crear grupos
dentro de la aplicación.
Justificación del
Requerimiento:
Fomentar la colaboración entre usuarios mediante la
formación de equipos con objetivos comunes.
Secuencia de Operaciones: 1. El usuario accede a la sección de grupos.
2. El usuario selecciona la opción de crear grupo.
3. El usuario ingresa nombre y descripción del grupo.
4. El sistema valida la información y registra el grupo.
Entradas Necesarias: El usuario inicia sesión en la aplicación.
Datos del grupo (nombre, descripción).
Salidas Generadas: Creación de un nuevo grupo en el sistema.
Relaciones entre Entradas y
Salidas:
Los datos ingresados por el usuario (entrada) permiten la
creación del grupo (salida).
Respuestas a Excepciones: Si los datos ingresados son inválidos o incompletos, el
sistema solicitará su corrección.
3.2.12 Requisito funcional 12
Nombre del Requerimiento: EcoSense App debe permitir a los usuarios unirse a grupos
existentes dentro de la aplicación.
Justificación del
Requerimiento:
Facilitar la participación de los usuarios en dinámicas
colaborativas dentro del sistema.
Secuencia de Operaciones: 1. El usuario accede a la sección de grupos.
2. El sistema muestra los grupos disponibles.
3. El usuario selecciona un grupo.
4. El sistema procesa la solicitud y agrega al usuario al
grupo.
Entradas Necesarias: El usuario inicia sesión en la aplicación.
Selección de un grupo disponible.
Salidas Generadas: El usuario pasa a ser miembro del grupo seleccionado.
Descripción de requisitos del software
Especificación de requisitos de software para la
aplicación Eco Sense App
Rev. 3.0
Pág. 28
Relaciones entre Entradas y
Salidas:
La selección de un grupo (entrada) permite la incorporación
del usuario al grupo (salida).
Respuestas a Excepciones: Si el grupo es privado, el sistema notificará que se requiere
aprobación para unirse.
3.2.13 Requisito funcional 13
Nombre del Requerimiento: EcoSense App debe mostrar un ranking individual
simplificado del usuario basado en los integrantes de su
grupo, indicando además la posición global de cada
miembro.
Justificación del
Requerimiento:
Permitir al usuario comparar su desempeño de manera más
directa con los miembros de su grupo, facilitando la
comprensión de su progreso sin necesidad de revisar el
ranking global completo.
Secuencia de Operaciones: 1. El usuario accede a la sección de ranking.
2. El usuario selecciona la vista de ranking por grupo.
3. El sistema obtiene los miembros del grupo del usuario.
4. El sistema ordena a los miembros según sus puntos
acumulados.
5. El sistema muestra el ranking interno del grupo junto con
la posición global de cada integrante.
Entradas Necesarias: El usuario inicia sesión en la aplicación.
Pertenencia del usuario a un grupo.
Salidas Generadas: Visualización del ranking individual simplificado dentro del
grupo, incluyendo la posición global de cada miembro.
Relaciones entre Entradas y
Salidas:
Los puntos acumulados de los miembros del grupo (entrada)
determinan su posición relativa dentro del grupo y su
posición global (salida).
Respuestas a Excepciones: Si el usuario no pertenece a un grupo, el sistema notificará
que debe unirse a uno para acceder a esta funcionalidad.
3.2.14 Requisito funcional 14
Nombre del Requerimiento: EcoSense App debe agregar automáticamente los puntos
obtenidos por un usuario al puntaje total del grupo al que
pertenece.
Justificación del
Requerimiento:
Asegurar la integración entre el progreso individual y el
desempeño grupal.
Secuencia de Operaciones: 1. El usuario realiza una acción de reciclaje.
Descripción de requisitos del software
Especificación de requisitos de software para la
aplicación Eco Sense App
Rev. 3.0
Pág. 29
2. El sistema asigna puntos al usuario.
3. El sistema identifica el grupo al que pertenece el usuario.
4. El sistema suma los puntos al total del grupo.
Entradas Necesarias: Acción de reciclaje realizada por el usuario.
Pertenencia del usuario a un grupo.
Salidas Generadas: Actualización del puntaje total del grupo.
Relaciones entre Entradas y
Salidas:
Los puntos obtenidos por el usuario (entrada) se reflejan en
el puntaje grupal (salida).
Respuestas a Excepciones: Si el usuario no pertenece a un grupo, los puntos se
acumulan únicamente en su cuenta personal.
3.2.15 Requisito funcional 15
Nombre del Requerimiento: EcoSense App debe otorgar recompensas grupales a los
grupos que alcancen metas de puntaje establecidas.
Justificación del
Requerimiento:
Incentivar la colaboración entre usuarios mediante objetivos
compartidos.
Secuencia de Operaciones: 1. El sistema define metas de puntaje para grupos.
2. El grupo acumula puntos a través de sus integrantes.
3. El sistema verifica si el grupo alcanza una meta.
4. El sistema habilita la recompensa grupal.
Entradas Necesarias: Puntos acumulados por el grupo.
Salidas Generadas: Disponibilidad de recompensas grupales para sus
integrantes.
Relaciones entre Entradas y
Salidas:
El puntaje acumulado del grupo (entrada) determina la
obtención de recompensas (salida).
Respuestas a Excepciones: Si el grupo no cumple con la meta requerida, el sistema
indicará el progreso restante.
3.2.16 Requisito funcional 16
Nombre del Requerimiento: EcoSense App debe permitir a los administradores gestionar
los miembros de un grupo.
Justificación del
Requerimiento:
Permitir el control y organización interna de los grupos.
Secuencia de Operaciones: 1. El administrador accede a la gestión del grupo.
2. El administrador visualiza los miembros del grupo.
Descripción de requisitos del software
Especificación de requisitos de software para la
aplicación Eco Sense App
Rev. 3.0
Pág. 30
3. El administrador selecciona acciones (agregar, eliminar o
modificar roles).
4. El sistema aplica los cambios realizados.
Entradas Necesarias: El usuario debe ser administrador de un grupo.
Acciones seleccionadas sobre miembros.
Salidas Generadas: Actualización de la composición del grupo.
Relaciones entre Entradas y
Salidas:
Las acciones del administrador (entrada) modifican la
estructura del grupo (salida).
Respuestas a Excepciones: Si el usuario no tiene permisos de administrador, el sistema
bloqueará la acción.
3.2.17 Requisito funcional 17
Nombre del Requerimiento: EcoSense App debe permitir a los usuarios visualizar la
posición de su grupo dentro del ranking global de grupos.
Justificación del
Requerimiento:
Mantener informados a los usuarios sobre el desempeño de
su grupo y fomentar la participación.
Secuencia de Operaciones: 1. El usuario accede a la sección de ranking grupal.
2. El sistema obtiene el ranking de grupos.
3. El sistema identifica el grupo del usuario.
4. El sistema muestra la posición del grupo dentro del
ranking.
Entradas Necesarias: El usuario inicia sesión en la aplicación.
Pertenencia a un grupo.
Salidas Generadas: Visualización de la posición del grupo en el ranking.
Relaciones entre Entradas y
Salidas:
Los puntos acumulados por el grupo (entrada) determinan su
posición en el ranking (salida).
Respuestas a Excepciones: Si el usuario no pertenece a un grupo, el sistema informará
que debe unirse a uno para visualizar esta información.
