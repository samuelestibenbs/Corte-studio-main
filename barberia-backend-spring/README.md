# API de Corte Studio (Spring Boot)

Microservicio REST en Java 17, Spring Boot y MongoDB. El backend existente en Node.js se conserva; este servicio es una alternativa para conectar la interfaz Angular.

## Arquitectura

- `domain`: modelos y reglas del negocio.
- `application`: casos de uso y puertos de salida.
- `infrastructure/adapter/in`: adaptador HTTP REST.
- `infrastructure/adapter/out`: persistencia MongoDB, seguridad y calendario.

Los controladores dependen de casos de uso; los casos de uso dependen de puertos, no de MongoDB ni de Spring Data. Los adaptadores MongoDB y JWT implementan esos puertos.

La propuesta de backlog, sprints y definición de terminado está en [SCRUM.md](SCRUM.md). Completa los registros de ceremonias con el trabajo real del equipo.

## Requisitos y ejecución

Se requiere Java 17 o superior. Maven Wrapper descarga y usa Maven automáticamente.

```sh
cd barberia-backend-spring
./mvnw spring-boot:run
```

Configura `MONGODB_URI`, `JWT_SECRET` y `ADMIN_EMAILS` como variables de entorno. `ADMIN_EMAILS` acepta correos separados por coma. El correo de cada barbero se asigna desde el catálogo del panel; al registrar una cuenta con ese email, el login recibe el rol de barbero. Para desarrollo, MongoDB local usa `mongodb://localhost:27017/corte_studio` si no se define `MONGODB_URI`.

La API escucha en `http://localhost:8080/api`. La clave JWT predeterminada es solo para desarrollo; define siempre una clave secreta propia fuera de entornos locales.

## Endpoints

- `GET /health`
- `POST /auth/register`
- `POST /auth/login`
- `POST /citas/agendar`
- `GET /citas/disponibilidad?fecha=AAAA-MM-DD&barbero=Nombre`
- `GET /catalogo` (público; se usa en portada y reserva)
- `GET /admin/catalogo` (administrador)
- `GET /admin/resumen` (administrador; devuelve únicamente conteos por barbero)
- `POST /admin/catalogo/barberos` (administrador; agrega perfil y email)
- `PUT /admin/catalogo/barberos/{id}` (administrador; edita perfil y disponibilidad)
- `DELETE /admin/catalogo/barberos/{id}` (administrador; elimina del catálogo)
- `PUT /admin/catalogo/servicios/{id}` (administrador; edita descripción y precio)
- `GET /barbero/citas` (barbero; solo sus citas asignadas)
- `PATCH /barbero/citas/{id}/realizada` (barbero; solo una cita asignada a su nombre)

El catálogo se inicializa con los datos actuales la primera vez y sus cambios persisten en MongoDB (`catalogos`). El endpoint público solo devuelve barberos disponibles. Al eliminar un perfil se conserva el historial de citas, pero esa cuenta deja de tener acceso como barbero. El sitio publicado mantiene el backend anterior hasta desplegar Spring Boot y actualizar la URL.

El servicio reutiliza las colecciones `citas` y `users` y el campo `password` de la base MongoDB anterior, por lo que cuentas y citas existentes pueden ser visibles si `MONGODB_URI` apunta a esa misma base.

Google Calendar está representado por un adaptador reemplazable que informa que no está configurado; la persistencia de citas no depende de esa integración.

## Pruebas

```sh
./mvnw test
```