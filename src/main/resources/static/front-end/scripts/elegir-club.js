document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session) return;
    const draft = session.perfilDtPendiente;
    if (!draft) { window.location.replace("crear-dt.html"); return; }

    const grid = document.getElementById("grid-clubes");
    const confirmButton = document.getElementById("btn-confirmar-club");
    const selectedName = document.getElementById("det-nombre");
    const selectedApodo = document.getElementById("det-apodo");
    const selectedDesc = document.getElementById("det-descripcion");
    const selectedSalary = document.getElementById("det-sueldo");
    const selectedBadge = document.getElementById("det-escudo");
    const stars = document.getElementById("det-estrellas");
    let equipos = [];
    let seleccionado = null;

    document.getElementById("btn-club-back").addEventListener("click", () => window.location.assign("crear-dt.html"));
    document.getElementById("btn-club-random").addEventListener("click", () => {
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
        card.className = "club-slot-card";
        card.dataset.id = equipo.equipoId;
        card.setAttribute("aria-label", equipo.nombre);
        card.innerHTML = `<span class="club-badge-img club-badge-fallback">${escapeHtml(equipo.nombre.slice(0, 2).toUpperCase())}</span>`;
        card.addEventListener("click", () => mostrarEquipo(equipo));
        return card;
    }

    function mostrarEquipo(equipo) {
        seleccionado = equipo;
        grid.querySelectorAll(".club-slot-card").forEach((card) => card.classList.toggle("selected", Number(card.dataset.id) === equipo.equipoId));
        selectedName.textContent = equipo.nombre.toUpperCase();
        selectedApodo.textContent = `Apodo: ${equipo.apodo}`;
        document.getElementById("det-estadio").textContent = `País: ${equipo.pais}`;
        document.getElementById("det-hinchada").textContent = `Prestigio: ${equipo.prestigio} estrellas`;
        document.getElementById("det-provincia").textContent = equipo.esInternacional ? "Equipo internacional" : "Equipo nacional";
        selectedDesc.textContent = `Comenzá tu carrera en ${equipo.nombre}. El equipo fue habilitado por el servidor según las estrellas iniciales de tu DT.`;
        selectedSalary.textContent = `$ ${Number(equipo.sueldo || 0).toLocaleString("es-AR")}`;
        const path = equipo.rutaEscudo ? `../assets/${equipo.rutaEscudo}` : "";
        selectedBadge.src = path;
        selectedBadge.alt = `Escudo de ${equipo.nombre}`;
        selectedBadge.classList.toggle("hidden", !path);
        stars.replaceChildren(...Array.from({ length: 5 }, (_, i) => {
            const star = document.createElement("span");
            star.className = i < equipo.prestigio ? "star-on" : "star-off";
            star.textContent = "★";
            return star;
        }));
        document.getElementById("lista-objetivos").textContent = "Iniciar temporada y competir";
        confirmButton.disabled = false;
        confirmButton.classList.remove("disabled");
    }

    function escapeHtml(value) {
        return String(value).replace(/[&<>"']/g, (char) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[char]));
    }
});
