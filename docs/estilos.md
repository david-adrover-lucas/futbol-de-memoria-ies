# Guia de estilos del front-end

## Estructura

- `src/main/resources/static/front-end/estilos/principal.css`: punto de entrada; importa las hojas por capas.
- `estilos/tokens.css`: colores, espacios, tamanos, radios, sombras y transiciones reutilizables.
- `estilos/base/`: reinicio, tipografia y estilos heredados que aun se conservan.
- `estilos/diseno/`: estructura general y distribucion de las pantallas.
- `estilos/componentes/`: botones, tarjetas, marcadores y elementos reutilizables.
- `estilos/paginas/`: reglas propias de cada pantalla.
- `estilos/utilidades/`: accesibilidad y clases auxiliares.
- `scripts/`: comportamiento de cada pantalla. `scripts/nucleo/` centraliza cliente HTTP, sesion, adaptadores y servicios.
- `pantallas/`: documentos HTML de cada vista. `assets/` conserva imagenes y recursos graficos del juego.

## Convenciones

Las clases CSS se escriben en espanol, en minusculas y separadas por guiones. Para componentes se usa BEM: `.bloque`, `.bloque__elemento` y `.bloque--modificador`. Los identificadores HTML tambien se escriben en espanol y se mantienen sincronizados con `getElementById` y los selectores de CSS. Los nombres de propiedades recibidos del backend y las rutas de la API conservan el contrato existente; no se traducen.

Los valores globales se declaran como propiedades personalizadas en `tokens.css` con nombres `--color-*`, `--espacio-*`, `--radio-*`, `--sombra-*` y `--transicion-*`. Evita repetir valores si existe un token equivalente.

Las hojas se cargan desde `principal.css` en este orden: reinicio, legado, tokens, base, diseno, componentes, paginas y utilidades. Para agregar estilos, usa la capa que corresponda y evita sumar reglas globales a `legado.css`; ese archivo sirve solo para estilos antiguos durante la migracion.

## Nombres principales migrados

| Antes | Ahora |
| --- | --- |
| `styles/main.css` | `estilos/principal.css` |
| `styles/layout/layout.css` | `estilos/diseno/diseno.css` |
| `styles/components/components.css` | `estilos/componentes/componentes.css` |
| `styles/pages/pages.css` | `estilos/paginas/paginas.css` |
| `styles/utilities/accessibility.css` | `estilos/utilidades/accesibilidad.css` |
| `style.css` | `estilos/base/legado.css` |
| `ui-system.css` | `estilos/componentes/sistema-interfaz.css` |
| `scripts/core/api-client.js` | `scripts/nucleo/cliente-api.js` |
| `scripts/core/session.js` | `scripts/nucleo/sesion.js` |
| `scripts/core/mappers.js` | `scripts/nucleo/adaptadores.js` |
| `scripts/core/services.js` | `scripts/nucleo/servicios.js` |
| `.menu-btn` | `.menu-boton` |
| `.answer-btn` | `.respuesta-boton` |
| `.match-screen` | `.partido-pantalla` |
| `--space-4` | `--espacio-4` |
| `--color-accent-cyan` | `--color-acento-cian` |

## Como modificar un componente

1. Busca sus clases en el HTML y sus reglas en `estilos/`.
2. Si renombras una clase o un ID, actualiza a la vez HTML, CSS y todas las referencias JS, incluidos `classList`, `dataset` y selectores.
3. Conserva los campos y rutas del backend tal como se reciben.
4. Comprueba la pantalla en escritorio y en una ventana angosta; verifica foco con teclado, estados vacios y carga de imagenes.
