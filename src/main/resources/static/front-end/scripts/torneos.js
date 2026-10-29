document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session) return;
    const api = window.FDM.services;
    const config = {
        libertadores: { title: "LIBERTADORES", logo: "../assets/logo-libertadores.png", cup: "../assets/copa-libertadores.png" },
        sudamericana: { title: "SUDAMERICANA", logo: "../assets/logo-sudamericana.png", cup: "../assets/copa-sudamericana.png" },
        nacional: { title: "NACIONAL", logo: "../assets/logo-nacional.png", cup: "../assets/copa-nacional.png" }
    };
    const selection = document.getElementById("selector-torneo");
    const bracket = document.getElementById("llave-torneo");
    const teams = document.getElementById("equipos-llave-torneo");
    const pestanasRonda = document.createElement("nav");
    pestanasRonda.className = "pestanas-rondas-torneo";
    pestanasRonda.setAttribute("aria-label", "Seleccionar ronda");
    pestanasRonda.setAttribute("role", "tablist");
    teams.before(pestanasRonda);
    let activeCode = null;

    try {
        const carrera = await api.carreraService.obtener(session.usuarioId);
        renderHeader(carrera);
        const summaries = await Promise.all(Object.keys(config).map(async (code) => {
            try { return [code, await api.torneoService.obtener(session.usuarioId, code)]; }
            catch { return [code, null]; }
        }));
        summaries.forEach(([code, data]) => renderizarEstadoSeleccion(code, data));
    } catch (error) { alert(error.message); return; }

    document.querySelectorAll(".torneo-big-boton").forEach((button) => button.addEventListener("click", async () => {
        const code = button.dataset.torneo;
        button.disabled = true;
        try {
            const tournament = await api.torneoService.obtener(session.usuarioId, code);
            activeCode = code;
            renderizarTorneo(tournament);
        } catch (error) { alert(error.message); }
        finally { button.disabled = false; }
    }));

    document.getElementById("boton-menu-torneo").addEventListener("click", () => {
        if (!bracket.classList.contains("oculto")) {
            bracket.classList.add("oculto"); selection.classList.remove("oculto");
            document.querySelector(".titulo-pagina-torneo").classList.remove("oculto");
            return;
        }
        window.location.assign("menu.html");
    });
    document.getElementById("boton-calendario-torneo").addEventListener("click", () => window.location.assign("temporada.html"));

    function renderizarEstadoSeleccion(code, data) {
        const button = document.querySelector(`.torneo-big-boton[data-torneo="${code}"]`);
        if (!button) return;
        let status = button.querySelector(".estado-selector-torneo");
        if (!status) { status = document.createElement("small"); status.className = "estado-selector-torneo"; button.appendChild(status); }
        status.textContent = !data || data.estado === "sin_iniciar" ? "SIN RESULTADOS"
            : data.campeon ? `CAMPEÓN: ${data.campeon.nombre}`
            : data.estado === "eliminado" ? (data.campeon ? "TU EQUIPO ELIMINADO · TORNEO FINALIZADO" : "TU EQUIPO ELIMINADO · CPU EN CURSO")
                    : `RONDA ${data.rondaActual} DE 4`;
    }

    function renderHeader(carrera) {
        document.getElementById("avatar-entrenador-torneo").src = `../assets/avatar-${carrera.numeroAvatar || 1}.png`;
        document.getElementById("nombre-entrenador-torneo").textContent = `${carrera.nombreDt} ${carrera.apellidoDt}`.toUpperCase();
        document.getElementById("estrellas-entrenador-torneo").textContent = "★".repeat(carrera.estrellas) + "☆".repeat(5 - carrera.estrellas);
        document.getElementById("nombre-club-torneo").textContent = carrera.equipoActual?.nombre?.toUpperCase() || "SIN EQUIPO";
        document.getElementById("insignia-club-torneo").src = carrera.equipoActual?.rutaEscudo ? `../assets/${carrera.equipoActual.rutaEscudo}` : "../assets/logo-futbolmemoria.png";
    }

    function renderizarTorneo(data) {
        selection.classList.add("oculto"); bracket.classList.remove("oculto");
        document.querySelector(".titulo-pagina-torneo").classList.add("oculto");
        const meta = config[data.codigoCompeticion];
        document.getElementById("titulo-llave-torneo").textContent = `${meta.title} · TEMPORADA ${data.numeroTemporada}`;
        const state = {
            sin_iniciar: "SIN RESULTADOS GUARDADOS",
            en_curso: `RONDA ${data.rondaActual} DE 4 · EN CURSO`,
            eliminado: data.campeon ? "TU EQUIPO ELIMINADO · TORNEO FINALIZADO" : "TU EQUIPO ELIMINADO · TORNEO EN SIMULACIÓN",
            campeon: "TORNEO FINALIZADO"
        };
        document.getElementById("ronda-llave-torneo").textContent = state[data.estado] || data.estado.toUpperCase();
        document.getElementById("logo-llave-torneo").src = meta.logo;
        document.getElementById("copa-llave-torneo").src = meta.cup;
        const champion = document.getElementById("campeon-llave-torneo");
        champion.textContent = data.campeon ? `CAMPEÓN: ${data.campeon.nombre}`
            : data.estado === "sin_iniciar" ? "La llave y los resultados aparecerán al avanzar las fechas del calendario." : "";
        teams.replaceChildren();
        pestanasRonda.replaceChildren();
        if (!data.fixtures.length) return;

        const nombresRonda = ["", "OCTAVOS", "CUARTOS", "SEMIFINAL", "FINAL"];
        const rondasMostradas = [];
        for (let round = 1; round <= 4; round += 1) {
            const fixtures = data.fixtures.filter((item) => item.ronda === round);
            if (!fixtures.length) continue;
            rondasMostradas.push(round);
            const column = document.createElement("section");
            column.className = `ronda-torneo ronda-torneo--${["", "octavos", "cuartos", "semifinal", "final"][round]}`;
            column.id = `ronda-torneo-${round}`;
            column.setAttribute("role", "tabpanel");
            column.setAttribute("aria-label", nombresRonda[round]);
            column.setAttribute("aria-labelledby", `pestana-ronda-torneo-${round}`);
            const title = document.createElement("h3"); title.textContent = nombresRonda[round]; column.appendChild(title);
            fixtures.forEach((fixture) => column.appendChild(renderizarEncuentro(fixture)));
            teams.appendChild(column);
        }
        const rondaSeleccionada = rondasMostradas.includes(data.rondaActual) ? data.rondaActual : rondasMostradas[0];
        rondasMostradas.forEach((round) => {
            const tab = document.createElement("button");
            tab.type = "button";
            tab.className = "pestana-ronda-torneo";
            tab.id = `pestana-ronda-torneo-${round}`;
            tab.setAttribute("role", "tab");
            tab.setAttribute("aria-controls", `ronda-torneo-${round}`);
            tab.setAttribute("aria-selected", String(round === rondaSeleccionada));
            tab.tabIndex = round === rondaSeleccionada ? 0 : -1;
            tab.textContent = nombresRonda[round];
            tab.addEventListener("click", () => seleccionarRonda(round));
            pestanasRonda.appendChild(tab);
        });
        seleccionarRonda(rondaSeleccionada);
    }

    function seleccionarRonda(round) {
        teams.querySelectorAll(".ronda-torneo").forEach((section) => {
            const active = section.id === `ronda-torneo-${round}`;
            section.classList.toggle("es-activo", active);
        });
        pestanasRonda.querySelectorAll("[role=tab]").forEach((tab) => {
            const active = tab.id === `pestana-ronda-torneo-${round}`;
            tab.setAttribute("aria-selected", String(active));
            tab.tabIndex = active ? 0 : -1;
        });
    }

    function renderizarEncuentro(fixture) {
        const article = document.createElement("article"); article.className = "partido-torneo";
        if (fixture.esPartidoDelUsuario) article.classList.add("mi-club");
        const left = document.createElement("strong"); left.textContent = fixture.equipoLocal.nombre;
        left.title = fixture.equipoLocal.nombre;
        const score = document.createElement("span"); score.className = "contra-torneo";
        score.textContent = fixture.estado === "finalizado" || fixture.estado === "pendiente_penales"
            ? `${fixture.golesLocal} - ${fixture.golesVisitante}${fixture.estado === "pendiente_penales" ? " · PEN" : ""}` : "VS";
        const right = document.createElement("strong"); right.textContent = fixture.equipoVisitante.nombre;
        right.title = fixture.equipoVisitante.nombre;
        if (fixture.equipoGanadorId != null || (fixture.estado === "finalizado" && fixture.golesLocal !== fixture.golesVisitante)) {
            const localGana = fixture.equipoGanadorId != null
                ? fixture.equipoGanadorId === fixture.equipoLocal.equipoId
                : fixture.golesLocal > fixture.golesVisitante;
            (localGana ? left : right).classList.add("es-ganador");
            (localGana ? right : left).classList.add("es-eliminado");
        }
        article.append(left, score, right);
        return article;
    }
});
