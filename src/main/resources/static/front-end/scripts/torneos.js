document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session) return;
    const api = window.FDM.services;
    const config = {
        libertadores: { title: "LIBERTADORES", logo: "../assets/logo-libertadores.png", cup: "../assets/copa-libertadores.png" },
        sudamericana: { title: "SUDAMERICANA", logo: "../assets/logo-sudamericana.png", cup: "../assets/copa-sudamericana.png" },
        nacional: { title: "NACIONAL", logo: "../assets/logo-nacional.png", cup: "../assets/copa-nacional.png" }
    };
    const selection = document.getElementById("tour-select");
    const bracket = document.getElementById("tour-bracket");
    const teams = document.getElementById("tour-bracket-teams");
    let activeCode = null;

    try {
        const carrera = await api.carreraService.obtener(session.usuarioId);
        renderHeader(carrera);
        const summaries = await Promise.all(Object.keys(config).map(async (code) => {
            try { return [code, await api.torneoService.obtener(session.usuarioId, code)]; }
            catch { return [code, null]; }
        }));
        summaries.forEach(([code, data]) => renderSelectionState(code, data));
    } catch (error) { alert(error.message); return; }

    document.querySelectorAll(".tour-big-btn").forEach((button) => button.addEventListener("click", async () => {
        const code = button.dataset.torneo;
        button.disabled = true;
        try {
            const tournament = await api.torneoService.obtener(session.usuarioId, code);
            activeCode = code;
            renderTournament(tournament);
        } catch (error) { alert(error.message); }
        finally { button.disabled = false; }
    }));

    document.getElementById("btn-tour-menu").addEventListener("click", () => {
        if (!bracket.classList.contains("hidden")) {
            bracket.classList.add("hidden"); selection.classList.remove("hidden");
            document.querySelector(".tour-page-title").classList.remove("hidden");
            return;
        }
        window.location.assign("menu.html");
    });
    document.getElementById("btn-tour-play").addEventListener("click", () => window.location.assign("temporada.html"));

    function renderSelectionState(code, data) {
        const button = document.querySelector(`.tour-big-btn[data-torneo="${code}"]`);
        if (!button) return;
        let status = button.querySelector(".tour-select-status");
        if (!status) { status = document.createElement("small"); status.className = "tour-select-status"; button.appendChild(status); }
        status.textContent = !data || data.estado === "sin_iniciar" ? "SIN RESULTADOS"
            : data.campeon ? `CAMPEÓN: ${data.campeon.nombre}`
            : data.estado === "eliminado" ? (data.campeon ? "TU EQUIPO ELIMINADO · TORNEO FINALIZADO" : "TU EQUIPO ELIMINADO · CPU EN CURSO")
                    : `RONDA ${data.rondaActual} DE 4`;
    }

    function renderHeader(carrera) {
        document.getElementById("tour-dt-avatar").src = `../assets/avatar-${carrera.numeroAvatar || 1}.png`;
        document.getElementById("tour-dt-name").textContent = `${carrera.nombreDt} ${carrera.apellidoDt}`.toUpperCase();
        document.getElementById("tour-dt-stars").textContent = "★".repeat(carrera.estrellas) + "☆".repeat(5 - carrera.estrellas);
        document.getElementById("tour-club-name").textContent = carrera.equipoActual?.nombre?.toUpperCase() || "SIN EQUIPO";
        document.getElementById("tour-club-badge").src = carrera.equipoActual?.rutaEscudo ? `../assets/${carrera.equipoActual.rutaEscudo}` : "../assets/logo-futbolmemoria.png";
    }

    function renderTournament(data) {
        selection.classList.add("hidden"); bracket.classList.remove("hidden");
        document.querySelector(".tour-page-title").classList.add("hidden");
        const meta = config[data.codigoCompeticion];
        document.getElementById("tour-bracket-title").textContent = `${meta.title} · TEMPORADA ${data.numeroTemporada}`;
        const state = {
            sin_iniciar: "SIN RESULTADOS GUARDADOS",
            en_curso: `RONDA ${data.rondaActual} DE 4 · EN CURSO`,
            eliminado: data.campeon ? "TU EQUIPO ELIMINADO · TORNEO FINALIZADO" : "TU EQUIPO ELIMINADO · TORNEO EN SIMULACIÓN",
            campeon: "TORNEO FINALIZADO"
        };
        document.getElementById("tour-bracket-round").textContent = state[data.estado] || data.estado.toUpperCase();
        document.getElementById("tour-bracket-logo").src = meta.logo;
        document.getElementById("tour-bracket-cup").src = meta.cup;
        const champion = document.getElementById("tour-bracket-champion");
        champion.textContent = data.campeon ? `CAMPEÓN: ${data.campeon.nombre}`
            : data.estado === "sin_iniciar" ? "La llave y los resultados aparecerán al avanzar las fechas del calendario." : "";
        teams.replaceChildren();
        if (!data.fixtures.length) return;

        const roundNames = ["", "OCTAVOS", "CUARTOS", "SEMIFINAL", "FINAL"];
        for (let round = 1; round <= 4; round += 1) {
            const fixtures = data.fixtures.filter((item) => item.ronda === round);
            if (!fixtures.length) continue;
            const column = document.createElement("section");
            column.className = `tour-round tour-round--${["", "octavos", "cuartos", "semis", "final"][round]}`;
            const title = document.createElement("h3"); title.textContent = roundNames[round]; column.appendChild(title);
            fixtures.forEach((fixture) => column.appendChild(renderFixture(fixture)));
            teams.appendChild(column);
        }
    }

    function renderFixture(fixture) {
        const article = document.createElement("article"); article.className = "tour-match";
        if (fixture.esPartidoDelUsuario) article.classList.add("my-club");
        const left = document.createElement("strong"); left.textContent = fixture.equipoLocal.nombre;
        const score = document.createElement("span"); score.className = "tour-vs";
        score.textContent = fixture.estado === "finalizado" || fixture.estado === "pendiente_penales"
            ? `${fixture.golesLocal} - ${fixture.golesVisitante}${fixture.estado === "pendiente_penales" ? " · PEN" : ""}` : "VS";
        const right = document.createElement("strong"); right.textContent = fixture.equipoVisitante.nombre;
        if (fixture.equipoGanadorId != null || (fixture.estado === "finalizado" && fixture.golesLocal !== fixture.golesVisitante)) {
            const localGana = fixture.equipoGanadorId != null
                ? fixture.equipoGanadorId === fixture.equipoLocal.equipoId
                : fixture.golesLocal > fixture.golesVisitante;
            (localGana ? left : right).classList.add("is-winner");
            (localGana ? right : left).classList.add("is-eliminated");
        }
        article.append(left, score, right);
        return article;
    }
});
