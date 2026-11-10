# Plan de trabajo Scrum

Este documento define una propuesta inicial. Completa los registros de cada sprint con lo que el equipo realmente planifique, construya y revise; no registres ceremonias ni resultados antes de que ocurran.

## Producto

Microservicio REST para que clientes reserven citas, barberos gestionen el estado de sus citas asignadas y administración consulte conteos agregados por barbero. La página Angular existente es el cliente del servicio.

## Roles

- Product Owner: prioriza las necesidades de clientes y administración.
- Scrum Master: facilita las ceremonias y ayuda a resolver impedimentos.
- Developers: diseñan, implementan, prueban y documentan el incremento.

En un equipo individual, registra quién asume cada responsabilidad.

## Product Backlog inicial

| Prioridad | Historia de usuario | Criterios de aceptación |
| --- | --- | --- |
| Alta | Como cliente, quiero reservar una cita con barbero, servicio, fecha y hora para asegurar mi espacio. | Una solicitud válida se guarda; los datos inválidos reciben HTTP 400; un horario duplicado recibe HTTP 409. |
| Alta | Como cliente, quiero consultar las horas ocupadas de un barbero en una fecha para elegir un horario disponible. | La API recibe `fecha` y `barbero` y responde JSON con `horariosOcupados`. |
| Alta | Como administrador, quiero iniciar sesión para acceder a las funciones de gestión. | Credenciales válidas devuelven un token; credenciales inválidas no lo devuelven. |
| Alta | Como barbero, quiero consultar mis citas y marcar una como realizada al terminar el servicio. | Solo puedo consultar y completar citas asignadas a mi identidad de barbero. |
| Alta | Como administrador, quiero consultar cuántos servicios realizó cada barbero para revisar el desempeño general. | La API devuelve conteos agregados por barbero, sin información de clientes ni citas individuales. |
| Media | Como administrador, quiero agregar, editar, desactivar y eliminar barberos y editar servicios para mantener actualizado el catálogo. | Los cambios se guardan en MongoDB, los no disponibles no aparecen en reservas y otras pestañas actualizan el catálogo. |
| Media | Como responsable técnico, quiero separar dominio, casos de uso y adaptadores para poder probar la lógica sin MongoDB. | Los casos de uso dependen de puertos y las pruebas usan adaptadores falsos. |
| Media | Como operador, quiero guardar datos en MongoDB y consultar la salud del servicio para mantener la API disponible. | La persistencia usa adaptadores; `/api/health` informa el estado real de la base. |
| Baja | Como cliente, quiero recibir una invitación de calendario al reservar para añadir la cita a mi agenda. | Se documenta y prueba el adaptador de calendario cuando se configuren sus credenciales. |

## Propuesta de sprints

Duración sugerida: dos semanas por sprint.

### Sprint 1: Reserva y persistencia

Objetivo: permitir consultar disponibilidad y reservar citas con persistencia en MongoDB.

Historias candidatas: reserva, consulta de disponibilidad, separación hexagonal y salud de base de datos.

### Sprint 2: Administración e integración

Objetivo: habilitar autenticación por rol, gestión de citas propias por barberos, conteos agregados para administración y conexión con Angular.

Historias candidatas: inicio de sesión, completar citas asignadas, resumen agregado, integración del frontend y adaptador de calendario si el alcance y las credenciales lo permiten.

## Definición de terminado

- Código integrado y compilable.
- Pruebas automatizadas relevantes aprobadas.
- Contrato REST y ejemplos de solicitudes/respuestas documentados.
- Variables sensibles configuradas fuera del repositorio.
- Incremento demostrado en la revisión del sprint.

## Registro por sprint

Para cada sprint, completa después de las ceremonias:

- Planificación: objetivo, historias seleccionadas y capacidad del equipo.
- Daily Scrum: progreso hacia el objetivo e impedimentos.
- Revisión: incremento demostrado y comentarios recibidos.
- Retrospectiva: qué funcionó, qué mejorar y acciones acordadas.
- Resultado: historias terminadas, pendientes y motivo de cualquier cambio.