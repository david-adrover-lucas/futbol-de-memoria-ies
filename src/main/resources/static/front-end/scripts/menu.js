document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session) return;
    const api = window.FDM.services;
    const panel = document.getElementById("menu-secondary-panel");
    const panelTitle = document.getElementById("menu-panel-title");
    const panelBody = document.getElementById("menu-panel-body");
    let [carrera, estadisticas] = await Promise.all([
        api.carreraService.obtener(session.usuarioId), api.estadisticaService.obtener(session.usuarioId)
    ]).catch((error) => { alert(error.message); return [null, null]; });
    if (!carrera) return;
    window.FDM.session.set({ ...session, carreraId: carrera.carreraId, carrera });
    renderPerfil(carrera);

    document.querySelectorAll(".menu-btn").forEach((button) => button.addEventListener("click", async () => {
        document.querySelectorAll(".menu-btn").forEach((item) => item.classList.remove("active"));
        button.classList.add("active");
        if (button.dataset.action === "jugar") {
            window.location.assign("temporada.html");
            return;
        }
        if (button.dataset.action) {
            const routes = { jugar: "partido.html", torneos: "torneos.html", temporada: "temporada.html", noticias: "perfil-dt.html" };
            if (routes[button.dataset.action]) window.location.assign(routes[button.dataset.action]);
            return;
        }
        await abrirPanel(button.dataset.panel);
    }));

    document.getElementById("btn-close-menu-panel").addEventListener("click", () => panel.classList.add("hidden"));
    document.getElementById("btn-save-game").addEventListener("click", () => alert("Tu carrera y los partidos se guardan automáticamente en la base de datos."));
    document.getElementById("btn-logout-menu").addEventListener("click", () => {
        window.FDM.session.clear();
        window.location.assign("../index.html");
    });

    async function abrirPanel(key) {
        panelBody.replaceChildren();
        panelTitle.textContent = key.toUpperCase();
        panel.classList.remove("hidden");
        try {
            if (key === "estadisticas") {
                estadisticas = await api.estadisticaService.obtener(session.usuarioId);
                renderRows([["Partidos jugados", estadisticas.jugados], ["Victorias", estadisticas.ganados],
                    ["Empates", estadisticas.empatados], ["Derrotas", estadisticas.perdidos],
                    ["Goles a favor", estadisticas.golesFavor], ["Goles en contra", estadisticas.golesContra]]);
            } else if (key === "objetivo") {
                renderRows([["Club actual", carrera.equipoActual?.nombre || "Sin equipo"],
                    ["Objetivo", carrera.objetivoContrato], ["Estado", carrera.estadoContrato],
                    ["Temporada", carrera.numeroTemporada]]);
            } else if (key === "ranking") {
                const page = await api.estadisticaService.ranking(0, 10);
                const own = (page.content || []).findIndex((item) => item.carreraId === carrera.carreraId);
                renderRows((page.content || []).map((item, i) => [`#${i + 1}`, `${item.nombreDt} ${item.apellidoDt} · ${item.experiencia} XP`])
                    .concat(own < 0 ? [["Tu posición", "Fuera de los primeros 10"]] : []));
            } else if (key === "ofertas") {
                const page = await api.equipoService.seleccionables(carrera.estrellas || 1);
                const choices = page.filter((team) => team.equipoId !== carrera.equipoActual?.equipoId).slice(0, 8);
                renderRows(choices.map((team) => [team.nombre, `${team.prestigio} estrellas · $ ${Number(team.sueldo).toLocaleString("es-AR")}`]));
            } else {
                panelTitle.textContent = "BANDEJA DE MENSAJES";
                renderEmpty("No hay mensajes guardados en el backend todavía.");
            }
        } catch (error) { renderEmpty(error.message); }
    }

    function renderPerfil(data) {
        const equipo = data.equipoActual || {};
        const nivel = Math.max(1, Number(data.estrellas) || 1);
        document.getElementById("menu-dt-avatar").src = `../assets/avatar-${data.numeroAvatar || 1}.png`;
        document.getElementById("menu-dt-name").textContent = `${data.nombreDt} ${data.apellidoDt}`.toUpperCase();
        document.getElementById("menu-dt-age").textContent = `${data.edadDt} AÑOS`;
        document.getElementById("menu-dt-province").textContent = (data.provincia || "--").toUpperCase();
        const stars = document.getElementById("menu-dt-stars");
        stars.replaceChildren(...Array.from({ length: 5 }, (_, i) => {
            const span = document.createElement("span");
            span.className = `menu-star ${i < nivel ? "is-filled" : "is-empty"}`;
            span.textContent = "★";
            return span;
        }));
        const exp = Number(data.experiencia) || 0;
        const floor = [0, 0, 500, 1500, 3000, 5000][nivel];
        const next = [500, 500, 1500, 3000, 5000, 5000][nivel];
        const progress = Math.min(100, Math.max(0, ((exp - floor) / Math.max(1, next - floor)) * 100));
        document.getElementById("menu-exp-bar").style.width = `${progress}%`;
        document.getElementById("menu-exp-label").textContent = `${exp} XP`;
        document.getElementById("menu-club-name").textContent = (equipo.nombre || "SIN EQUIPO").toUpperCase();
        document.getElementById("menu-club-apodo").textContent = (equipo.apodo || "--").toUpperCase();
        document.getElementById("menu-club-badge").src = equipo.rutaEscudo ? `../assets/${equipo.rutaEscudo}` : "../assets/logo-futbolmemoria.png";
    }

    function renderRows(rows) {
        if (!rows.length) return renderEmpty("No hay datos para mostrar.");
        rows.forEach(([label, value]) => {
            const row = document.createElement("div");
            row.className = "menu-info-row";
            const title = document.createElement("span"); title.textContent = label;
            const text = document.createElement("strong"); text.textContent = value ?? "0";
            row.append(title, text); panelBody.appendChild(row);
        });
    }
    function renderEmpty(message) { const item = document.createElement("p"); item.textContent = message; panelBody.appendChild(item); }
});
