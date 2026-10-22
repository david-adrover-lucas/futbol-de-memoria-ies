# Fútbol de Memoria

Frontend HTML/CSS/JavaScript y API Spring Boot en la misma aplicación. Se sirven desde el mismo origen: el frontend usa `/api` y no requiere CORS mientras se abra desde Spring.

## Preparar la base local

Con MariaDB/MySQL iniciado en XAMPP, ejecutar una sola vez y en este orden los scripts de `src/main/resources/db/manual/`:

1. `schema.sql`
2. `extension_partidos.sql`
3. `extension_carrera.sql`
4. `seed.sql`

`seed.sql` carga los catálogos y preguntas; no volver a ejecutarlo sobre una base que ya tiene esos datos.

## Iniciar la aplicación

Desde PowerShell, en la carpeta del proyecto:

```powershell
$env:DB_URL = "jdbc:mariadb://localhost:3306/futbol_memoria"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = ""
.\mvnw.cmd spring-boot:run
```

Abrir `http://localhost:8080/front-end/index.html`. El archivo `.env.example` documenta las variables, pero Spring Boot no carga archivos `.env` automáticamente; hay que definirlas en el proceso o en el entorno del IDE. Para otra URL de API, cambiar el `meta[name="api-base-url"]` de las páginas HTML. Si frontend y API se sirven desde dominios distintos, configurar el origen permitido en Spring antes de desplegarlos separados.

## Estructura

- `src/main/java/.../controllers`, `services`, `repository`, `models`: API y persistencia.
- `src/main/resources/static/front-end`: páginas y recursos visuales.
- `static/front-end/scripts/core`: cliente HTTP, sesión, mappers y servicios frontend.
- `src/main/resources/db/manual`: scripts SQL manuales.
- `src/test/js`: pruebas de cliente HTTP, servicios y mappers (Node.js integrado, sin paquetes adicionales).

## Pruebas

```powershell
node --test --test-isolation=none src/test/js/*.test.js
.\mvnw.cmd test
```

La sesión es local de demo y conserva el `usuarioId` en `sessionStorage`; no implementa autenticación segura.
