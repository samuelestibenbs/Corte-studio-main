# CORTE STUDIO · Angular + API Spring Boot

#Frontend

npm install
npm start        # http://localhost:4200

- Marca y textos: `src/app/core/brand.ts`
- Servicios, barberos y horarios visibles: `src/app/core/demo-data.ts`
- Tokens de diseño: bloque `:root` de `src/styles.css`
- Fotos reales: pasa `[src]="'assets/...'"` a `<app-photo>` (archivos en `public/`)
- Rutas: `/`, `/reservar`, `/ingresar`.

## API

El frontend depende del puerto `src/app/core/ports/barberia-api.port.ts`; la clase
`BarberiaApi` lo implementa como adaptador HTTP.

La implementación local en Java está en `barberia-backend-spring` y separa dominio,
aplicación y adaptadores de infraestructura. `barberia-backend-main` es la API Express
anterior que se conserva para el despliegue existente.

Para desplegar Spring Boot y Angular en Render, sigue [DEPLOYMENT.md](DEPLOYMENT.md).

Para ejecutar las pruebas unitarias de Spring Boot:

```sh
cd barberia-backend-spring
./mvnw test
```

La interfaz local consume `http://localhost:8080/api`. El sitio publicado todavía usa la API Express anterior hasta desplegar Spring Boot y actualizar la URL.

Para desarrollo local se requiere Java 17+ y Docker. Inicia MongoDB una vez:

```sh
docker run --name corte-mongo -p 27017:27017 -v corte-mongo-data:/data/db -d mongo:8
```

Luego, desde `barberia-backend-spring`, inicia la API. Ajusta correos reales si los tienes:

```sh
env MONGODB_URI='mongodb://localhost:27017/corte_studio' \
   JWT_SECRET='clave-solo-local-cambiar' \
   ADMIN_EMAILS='admin@cortestudio.test' \
   ./mvnw spring-boot:run
```

En otra terminal, desde la raíz del proyecto, ejecuta `npm start` para abrir Angular en `http://localhost:4200`. La API guarda nombre, servicio, barbero, fecha y hora de cada cita; no recoge ni almacena el teléfono. La disponibilidad se consulta por barbero y fecha.

## Administración

1. Configura `ADMIN_EMAILS` con los correos de administración. Los barberos se agregan en el panel indicando el correo que usarán para su cuenta.
2. Registra una cuenta con el correo del barbero desde `/ingresar` para asignarle acceso a sus citas.
3. En `/admin`, cada barbero ve sus citas y puede marcarlas como realizadas. La cuenta administrativa ve únicamente los conteos de cortes realizados por barbero; además puede agregar/eliminar barberos, cambiar su disponibilidad, editar sus perfiles y modificar la descripción y precio de cada servicio. Los cambios se guardan en MongoDB y se actualizan en portada y reserva, incluso en otras pestañas abiertas.

El resumen requiere un token de una cuenta cuyo correo esté configurado en `ADMIN_EMAILS`. Los endpoints del barbero solo permiten actuar sobre citas asignadas al nombre configurado para esa cuenta.

## Invitaciones de Google Calendar

Cuando se agenda una cita, el backend intenta crear un evento en el calendario del negocio e invita por correo al barbero elegido. La duración del evento corresponde al servicio y la zona horaria es `America/Bogota`. Si Calendar todavía no está configurado o falla el envío, la cita se guarda y la pantalla informa que la invitación no fue enviada.

Para activarlo:

1. Crea un calendario para la barbería desde una cuenta de Google y copia su ID en la configuración del calendario.
2. En Google Cloud, crea un proyecto, habilita Google Calendar API y crea una cuenta de servicio.
3. Comparte el calendario del negocio con el correo de la cuenta de servicio y dale permiso para hacer cambios en los eventos.
4. En las variables de entorno del backend (el `.env` local y el servicio de Render), configura `GOOGLE_CALENDAR_ID`, `GOOGLE_SERVICE_ACCOUNT_JSON` y `BARBER_EMAILS_JSON`. El último es un objeto JSON con las claves `samuel`, `luis` y `johan` y el correo de Google que debe recibir cada invitación.
5. Guarda el JSON privado de la cuenta de servicio únicamente como variable secreta del servidor; no lo subas al repositorio ni lo compartas por chat. Vuelve a desplegar el backend y prueba una reserva.

La configuración de ejemplo está en `barberia-backend-main/.env.example`. Google Calendar enviará una invitación al correo configurado; la persona debe aceptar la invitación para que aparezca como cita confirmada en su calendario personal.
