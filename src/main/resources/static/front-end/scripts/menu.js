document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session) return;
    const api = window.FDM.services;
    const panel = document.getElementById("menu-secondary-panel");
    const panelTitle = document.getElementById("menu-panel-title");
    const panelBody = document.getElementById("menu-panel-body");
    const playButton = document.getElementById("btn-menu-play");
    const calendarInfo = document.getElementById("menu-calendar-info");
    let [carrera, estadisticas] = await Promise.all([
        api.carreraService.obtener(session.usuarioId), api.estadisticaService.obtener(session.usuarioId)
    ]).catch((error) => { alert(error.message); return [null, null]; });
    if (!carrera) return;
    let calendar;
    try {
        calendar = await api.calendarioService.actual(session.usuarioId);
        renderCalendar(calendar);
    } catch (error) {
        playButton.disabled = true;
        playButton.textContent = "CALENDARIO NO DISPONIBLE";
        calendarInfo.textContent = error.message;
    }
    window.FDM.session.set({ ...session, carreraId: carrera.carreraId, carrera });
    renderPerfil(carrera);

    document.querySelectorAll(".menu-btn").forEach((button) => button.addEventListener("click", async () => {
        document.querySelectorAll(".menu-btn").forEach((item) => item.classList.remove("active"));
        button.classList.add("active");
        if (button.dataset.action === "jugar") {
            await jugarOAvanzarFecha(button);
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

    async function jugarOAvanzarFecha(button) {
        if (!calendar) return;
        button.disabled = true;
        try {
            if (calendar.esFechaLibre) {
                await api.calendarioService.avanzar(session.usuarioId);
                window.location.reload();
                return;
            }
            const codigo = calendar.codigoCompeticionActual;
            let fixture;
            if (codigo === "temporada") {
                fixture = await api.temporadaService.siguientePartido(session.usuarioId);
            } else {
                await api.torneoService.iniciar(session.usuarioId, codigo);
                fixture = await api.torneoService.partido(session.usuarioId, codigo);
            }
            const game = fixture.partidoId ? { partidoId: fixture.partidoId }
                : await api.partidoService.iniciar(session.usuarioId, codigo,
                    codigo === "temporada" ? { temporadaFixtureId: fixture.fixtureId } : { torneoFixtureId: fixture.fixtureId });
            window.FDM.session.set({ ...session, carreraId: carrera.carreraId, carrera,
                competicionCodigo: codigo, partidoActualId: game.partidoId });
            window.location.assign(`partido.html?competicion=${encodeURIComponent(codigo)}`);
        } catch (error) {
            alert(error.message);
            button.disabled = false;
        }
    }

    function renderCalendar(data) {
        const competition = data.nombreCompeticionActual || data.codigoCompeticionActual || "Competición";
        const round = data.nombreRondaActual ? ` · ${data.nombreRondaActual}` : "";
        const date = `Temporada ${data.numeroTemporada} · Fecha ${data.fechaActual}/${data.totalFechas}`;
        calendarInfo.textContent = data.esFechaLibre
            ? `${date} · Fecha libre: al avanzar se simulan los partidos de CPU.`
            : `${date} · ${competition}${round}`;
        playButton.textContent = data.esFechaLibre ? "AVANZAR FECHA" : `JUGAR ${competition}`;
    }

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
