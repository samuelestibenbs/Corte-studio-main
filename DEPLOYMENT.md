# Despliegue en Render

El repositorio incluye `render.yaml` para publicar la API Spring Boot y Angular como dos servicios. La base MongoDB se crea en MongoDB Atlas y se conecta como secreto; no se guarda ninguna credencial en Git.

## 1. Preparar MongoDB Atlas

1. Crea un clúster de MongoDB y un usuario de base de datos con permisos de lectura/escritura para `corte_studio`.
2. Copia la URI de conexión y reemplaza los marcadores de usuario, contraseña y base de datos. Codifica los caracteres reservados de la contraseña para URL.
3. Configura el acceso de red para que Render pueda conectarse. En un plan Render sin IP de salida fija, Atlas puede requerir permitir `0.0.0.0/0`; usa una contraseña robusta y limita el usuario a esta base. Para producción con requisitos estrictos, usa salida de red fija o conectividad privada.

## 2. Crear los servicios

1. Sube el repositorio a GitHub, GitLab o Bitbucket.
2. En Render, crea un Blueprint y selecciona el repositorio. Render leerá el archivo `render.yaml` y propondrá `corte-studio-api-spring` y `corte-studio-web`.
3. Cuando Render lo solicite, proporciona `MONGODB_URI` y `ADMIN_EMAILS`. Usa el correo real de la persona que administrará el catálogo. Render genera `JWT_SECRET` automáticamente.
4. Espera a que ambos servicios terminen el primer despliegue. La API debe pasar el health check `/api/health`; el sitio quedará disponible en `https://corte-studio-web.onrender.com`.

El backend usa el perfil `prod`, que exige `MONGODB_URI`, `JWT_SECRET`, `ADMIN_EMAILS` y `APP_CORS_ALLOWED_ORIGINS`. No reemplaces `JWT_SECRET` por una clave de ejemplo. Si cambias el dominio del frontend, actualiza `APP_CORS_ALLOWED_ORIGINS` en Render para que coincida exactamente con su origen.

## 3. Crear las cuentas y el catálogo

1. Registra la cuenta administrativa desde `/ingresar` usando uno de los correos de `ADMIN_EMAILS`.
2. Inicia sesión y abre `/admin`.
3. El catálogo se inicializa con los barberos de demostración. Elimínalos o desactívalos y agrega los perfiles reales con sus correos.
4. Cada barbero debe registrar una cuenta con el mismo correo guardado en su perfil; el backend le asigna el rol de barbero al iniciar sesión.
5. Revisa precios y descripciones, crea una reserva de prueba y confirma que aparece en el panel del barbero.

Google Calendar no está integrado en la API Spring Boot actual: las reservas se guardan, pero no se envían invitaciones de calendario. El plan gratuito de Render puede suspender servicios inactivos y demorar la primera solicitud al reactivarse; para disponibilidad continua, selecciona un plan que no suspenda el servicio.

## Comprobaciones antes de anunciar el sitio

- `https://corte-studio-api-spring.onrender.com/api/health` devuelve `200` y `database: connected`.
- La página pública carga catálogo desde Spring Boot, no desde la API Express anterior.
- Inicio de sesión, reserva, disponibilidad, gestión por barbero y edición del catálogo funcionan en el dominio publicado.
- `ADMIN_EMAILS`, `JWT_SECRET` y `MONGODB_URI` están definidos como variables de entorno secretas en Render.
- El cambio de precios se verifica en la base publicada y en una sesión nueva de reserva.