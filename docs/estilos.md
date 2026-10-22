# Guía de estilos del frontend

El frontend usa HTML, CSS y JavaScript sin framework. Todas las páginas cargan
src/main/resources/static/front-end/styles/main.css, que importa los estilos
en capas: reset, hojas históricas, tokens, base, layout, componentes, páginas y
accesibilidad. Las hojas style.css y ui-system.css siguen activas porque
contienen selectores y estados que todavía utilizan las pantallas; no se deben
borrar hasta comprobar que ninguna página ni script depende de ellos.

## Cambiar el tema

Los valores comunes están en styles/tokens.css bajo :root. Para cambiar
colores, radios, sombras, tipografía o tamaños táctiles, edita esos tokens. Un
tema alternativo puede declarar los mismos tokens en un atributo, por ejemplo:

~~~css
[data-theme="stadium-day"] {
    --color-background: #eef4f8;
    --color-surface: #fff;
    --color-surface-raised: #e3edf3;
    --color-text-primary: #10243a;
    --color-text-secondary: #3d5268;
    --color-accent-cyan: #087b9b;
    --color-accent-gold: #835300;
}
~~~

Se conservan alias --ui-* mientras las reglas históricas los necesiten.

## Añadir o modificar estilos

- Reglas generales del documento: styles/base/.
- Estructura y distribución: styles/layout/layout.css.
- Controles y superficies reutilizables: styles/components/components.css.
- Ajustes propios de una pantalla: styles/pages/pages.css.
- Foco, interacción táctil, alto contraste y movimiento reducido:
  styles/utilities/accessibility.css.

Usa los nombres de clase que ya genera el HTML y que consulta JavaScript. No
renombres IDs ni clases usadas por selectores de scripts sin migrarlos juntos.
Para un componente nuevo, prefiere una clase propia y tokens; evita selectores
globales que cambien botones o tarjetas de otras pantallas.

## Responsive y accesibilidad

Las reglas actuales cubren móvil (600 px), tablet (hasta 920/980 px), escritorio
y pantallas amplias; las llaves y la tabla de temporada tienen desplazamiento
horizontal cuando no caben. Los controles interactivos mantienen una altura
mínima de 44 px. El foco de teclado usa :focus-visible; el hover se limita a
dispositivos que realmente lo admiten; se respetan prefers-reduced-motion,
forced-colors y las áreas seguras de dispositivos móviles.

Antes de publicar cambios visuales, revisa en Chrome, Firefox, Edge y Safari
(incluido Safari iOS) los anchos 360, 390, 768, 1024, 1440 y 1920 px, y el
formato horizontal en móvil. Comprueba navegación con teclado, zoom del
navegador, contraste y que no aparezca scroll horizontal fuera de tablas/llaves.
