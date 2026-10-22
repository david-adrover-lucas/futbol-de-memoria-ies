# Futbol de Memoria

Aplicacion local con frontend HTML/CSS/JavaScript y API Spring Boot en el mismo proyecto. Spring sirve las paginas y el cliente usa `/api` en el mismo origen.

## Preparar MySQL/MariaDB

Con XAMPP iniciado, ejecutar una vez y en este orden los scripts de `src/main/resources/db/manual/`:

1. `schema.sql`
2. `extension_partidos.sql`
3. `extension_carrera.sql`
4. `extension_clasificacion.sql`
5. `extension_penales.sql`
6. `extension_calendario.sql`
7. `seed.sql`

Si la base ya existe, ejecutar solo las extensiones que falten. Para esta fase, ejecutar `extension_calendario.sql` una sola vez despues de `extension_carrera.sql`, `extension_clasificacion.sql` y `extension_penales.sql`. Crea el calendario persistido de 41 fechas y permite guardar las copas simuladas por CPU. No volver a ejecutar `seed.sql` si ya se cargaron los catalogos y preguntas.

## Iniciar la aplicacion

En PowerShell, desde esta carpeta:

```powershell
$env:DB_URL = "jdbc:mariadb://localhost:3306/futbol_memoria"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = ""
.\mvnw.cmd spring-boot:run
```

Abrir `http://localhost:8080/front-end/index.html`. Spring Boot no carga `.env` automaticamente; definir las variables en PowerShell o en la configuracion del IDE.

## Calendario de temporada

La temporada tiene 29 jornadas de liga y cuatro rondas para cada copa. El endpoint `GET /api/usuarios/{usuarioId}/calendario` devuelve la fecha vigente; `POST /api/usuarios/{usuarioId}/calendario/avanzar` simula y guarda la ronda de CPU en fechas libres. El frontend muestra la accion disponible en la pantalla de temporada.

La pantalla Torneos consulta los resultados guardados con `GET /api/usuarios/{usuarioId}/torneos/{codigo}`. Consultar no inicia un torneo ni altera resultados; las llaves y campeones aparecen a medida que se disputan o simulan sus fechas.

## Estructura

- `src/main/java/.../controllers`, `services`, `repository`, `models`: API y persistencia.
- `src/main/resources/static/front-end`: paginas y recursos visuales.
- `static/front-end/scripts/core`: cliente HTTP, sesion, mappers y servicios del frontend.
- `src/main/resources/db/manual`: scripts SQL manuales.
- `src/test`: pruebas Java y JavaScript.

## Pruebas

```powershell
node --test --test-isolation=none src/test/js/*.test.js
.\mvnw.cmd test
```

La sesion es local de demo y guarda el `usuarioId` en `sessionStorage`; todavia no implementa autenticacion segura.
