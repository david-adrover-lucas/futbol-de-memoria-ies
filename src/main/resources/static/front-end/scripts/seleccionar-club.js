document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session) return;
    const draft = session.perfilDtPendiente;
    if (!draft) { window.location.replace("crear-dt.html"); return; }

    const grid = document.getElementById("grilla-clubes");
    const confirmButton = document.getElementById("boton-confirmar-club");
    const selectedName = document.getElementById("detalle-nombre");
    const selectedApodo = document.getElementById("detalle-apodo");
    const selectedDesc = document.getElementById("detalle-descripcion");
    const selectedSalary = document.getElementById("detalle-sueldo");
    const selectedBadge = document.getElementById("detalle-escudo");
    const stars = document.getElementById("detalle-estrellas");
    let equipos = [];
    let seleccionado = null;

    document.getElementById("boton-volver-club").addEventListener("click", () => window.location.assign("crear-dt.html"));
    document.getElementById("boton-club-aleatorio").addEventListener("click", () => {
        if (equipos.length) mostrarEquipo(equipos[Math.floor(Math.random() * equipos.length)]);
    });
    confirmButton.addEventListener("click", async () => {
        if (!seleccionado || confirmButton.disabled) return;
        confirmButton.disabled = true;
        try {
            const carrera = await window.FDM.services.carreraService.crear(session.usuarioId, {
                ...draft,
                equipoInicialId: seleccionado.equipoId
            });
            window.FDM.session.set({ usuarioId: session.usuarioId, nombreUsuario: session.nombreUsuario,
                carreraId: carrera.carreraId, carrera });
            window.location.assign("menu.html");
        } catch (error) {
            alert(error.message);
            confirmButton.disabled = false;
        }
    });

    try {
        equipos = await window.FDM.services.equipoService.seleccionables(1);
        if (!equipos.length) throw new Error("No hay equipos iniciales. Comprueba que hayas ejecutado seed.sql.");
        grid.replaceChildren(...equipos.map(crearTarjeta));
        mostrarEquipo(equipos[0]);
    } catch (error) {
        grid.textContent = error.message;
    }

    function crearTarjeta(equipo) {
        const card = document.createElement("button");
        card.type = "button";
        card.className = "tarjeta-casillero-club";
        card.dataset.id = equipo.equipoId;
        card.setAttribute("aria-label", `Seleccionar ${equipo.nombre}, ${equipo.prestigio} estrellas`);
        card.setAttribute("aria-pressed", "false");

        const badge = document.createElement("span");
        badge.className = "insignia-casillero-club";
        const imagePath = equipo.rutaEscudo ? `../assets/${equipo.rutaEscudo}` : "";
        if (imagePath) {
            const image = document.createElement("img");
            image.className = "imagen-insignia-club";
            image.src = imagePath;
            image.alt = "";
            image.addEventListener("error", () => {
                const fallback = document.createElement("span");
                fallback.className = "imagen-insignia-club insignia-club-alternativa";
                fallback.textContent = equipo.nombre.slice(0, 2).toUpperCase();
                badge.replaceChildren(fallback);
            }, { once: true });
            badge.appendChild(image);
        } else {
            const fallback = document.createElement("span");
            fallback.className = "imagen-insignia-club insignia-club-alternativa";
            fallback.textContent = equipo.nombre.slice(0, 2).toUpperCase();
            badge.appendChild(fallback);
        }

        const name = document.createElement("span");
        name.className = "nombre-casillero-club";
        name.textContent = equipo.nombre;

        const prestige = document.createElement("span");
        prestige.className = "estrellas-casillero-club";
        prestige.textContent = "★".repeat(equipo.prestigio) + "☆".repeat(5 - equipo.prestigio);
        prestige.setAttribute("aria-label", `${equipo.prestigio} de 5 estrellas`);

        card.append(badge, name, prestige);
        card.addEventListener("click", () => mostrarEquipo(equipo));
        return card;
    }

    function mostrarEquipo(equipo) {
        seleccionado = equipo;
        grid.querySelectorAll(".tarjeta-casillero-club").forEach((card) => {
            const selected = Number(card.dataset.id) === equipo.equipoId;
            card.classList.toggle("seleccionado", selected);
            card.setAttribute("aria-pressed", String(selected));
        });
        selectedName.textContent = equipo.nombre.toUpperCase();
        selectedApodo.textContent = `Apodo: ${equipo.apodo}`;
        document.getElementById("detalle-estadio").textContent = `País: ${equipo.pais}`;
        document.getElementById("detalle-hinchada").textContent = `Prestigio: ${equipo.prestigio} estrellas`;
        document.getElementById("detalle-provincia").textContent = equipo.esInternacional ? "Equipo internacional" : "Equipo nacional";
        selectedDesc.textContent = `Comenzá tu carrera en ${equipo.nombre}. El equipo fue habilitado por el servidor según las estrellas iniciales de tu DT.`;
        selectedSalary.textContent = `$ ${Number(equipo.sueldo || 0).toLocaleString("es-AR")}`;
        const path = equipo.rutaEscudo ? `../assets/${equipo.rutaEscudo}` : "";
        selectedBadge.src = path;
        selectedBadge.alt = `Escudo de ${equipo.nombre}`;
        selectedBadge.classList.toggle("oculto", !path);
        selectedBadge.onerror = () => selectedBadge.classList.add("oculto");
        stars.replaceChildren(...Array.from({ length: 5 }, (_, i) => {
            const star = document.createElement("span");
            star.className = i < equipo.prestigio ? "estrella-encendido" : "estrella-apagado";
            star.textContent = "★";
            return star;
        }));
        document.getElementById("lista-objetivos").textContent = "Iniciar temporada y competir";
        confirmButton.disabled = false;
        confirmButton.classList.remove("deshabilitado");
    }

});
