document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session) return;
    const config = {
        libertadores: { title: "LIBERTADORES", logo: "../assets/logo-libertadores.png", cup: "../assets/copa-libertadores.png" },
        sudamericana: { title: "SUDAMERICANA", logo: "../assets/logo-sudamericana.png", cup: "../assets/copa-sudamericana.png" },
        nacional: { title: "NACIONAL", logo: "../assets/logo-nacional.png", cup: "../assets/copa-nacional.png" }
    };
    const selection = document.getElementById("tour-select");
    const bracket = document.getElementById("tour-bracket");
    const teams = document.getElementById("tour-bracket-teams");
    let activeCode = null;

    try { renderHeader(await window.FDM.services.carreraService.obtener(session.usuarioId)); }
    catch (error) { alert(error.message); return; }

    document.querySelectorAll(".tour-big-btn").forEach((button) => button.addEventListener("click", async () => {
        const code = button.dataset.torneo;
        button.disabled = true;
        try {
            const tournament = await window.FDM.services.torneoService.iniciar(session.usuarioId, code);
            activeCode = code; renderTournament(tournament);
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
    document.getElementById("btn-tour-play").addEventListener("click", async () => {
        if (!activeCode) return alert("Seleccioná una copa para ver su llave.");
        try {
            const fixture = await window.FDM.services.torneoService.partido(session.usuarioId, activeCode);
            const game = fixture.partidoId ? { partidoId: fixture.partidoId }
                : await window.FDM.services.partidoService.iniciar(session.usuarioId, activeCode, { torneoFixtureId: fixture.fixtureId });
            window.FDM.session.set({ ...session, competicionCodigo: activeCode, partidoActualId: game.partidoId });
            window.location.assign(`partido.html?competicion=${encodeURIComponent(activeCode)}`);
        } catch (error) { alert(error.message); }
    });

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
        document.getElementById("tour-bracket-round").textContent = data.estado === "en_curso" ? `RONDA ${data.rondaActual} DE 4` : data.estado.toUpperCase();
        document.getElementById("tour-bracket-logo").src = meta.logo;
        document.getElementById("tour-bracket-cup").src = meta.cup;
        teams.replaceChildren();
        const roundNames = ["", "OCTAVOS", "CUARTOS", "SEMIFINAL", "FINAL"];
        for (let round = 1; round <= 4; round += 1) {
            const fixtures = data.fixtures.filter((item) => item.ronda === round);
            if (!fixtures.length) continue;
            const column = document.createElement("section"); column.className = `tour-round tour-round--${["", "octavos", "cuartos", "semis", "final"][round]}`;
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
        score.textContent = fixture.estado === "finalizado" ? `${fixture.golesLocal} - ${fixture.golesVisitante}` : "VS";
        const right = document.createElement("strong"); right.textContent = fixture.equipoVisitante.nombre;
        article.append(left, score, right);
        return article;
    }
});
