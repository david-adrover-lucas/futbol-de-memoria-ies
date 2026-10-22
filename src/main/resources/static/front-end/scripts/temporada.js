document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session) return;
    const playButton = document.getElementById("btn-season-play");
    let temporada;
    try {
        const carrera = await window.FDM.services.carreraService.obtener(session.usuarioId);
        temporada = await window.FDM.services.temporadaService.obtener(session.usuarioId);
        renderHeader(carrera);
        renderTabla(temporada.tabla, carrera.equipoActual?.equipoId);
        document.querySelector(".screen-title-plate").textContent = `TEMPORADA ${temporada.numeroTemporada} · JORNADA ${temporada.jornadaActual}/${temporada.jornadasTotales}`;
    } catch (error) { alert(error.message); return; }

    document.getElementById("btn-season-menu").addEventListener("click", () => window.location.assign("menu.html"));
    playButton.addEventListener("click", async () => {
        playButton.disabled = true;
        try {
            const fixture = await window.FDM.services.temporadaService.siguientePartido(session.usuarioId);
            let game;
            if (fixture.partidoId) game = { partidoId: fixture.partidoId };
            else game = await window.FDM.services.partidoService.iniciar(session.usuarioId, "temporada", { temporadaFixtureId: fixture.fixtureId });
            window.FDM.session.set({ ...session, competicionCodigo: "temporada", partidoActualId: game.partidoId });
            window.location.assign("partido.html?competicion=temporada");
        } catch (error) { alert(error.message); playButton.disabled = false; }
    });

    function renderHeader(carrera) {
        document.getElementById("season-dt-avatar").src = `../assets/avatar-${carrera.numeroAvatar || 1}.png`;
        document.getElementById("season-dt-name").textContent = `${carrera.nombreDt} ${carrera.apellidoDt}`.toUpperCase();
        document.getElementById("season-dt-age").textContent = `EDAD: ${carrera.edadDt}`;
        document.getElementById("season-dt-province").textContent = `PROVINCIA: ${carrera.provincia}`;
        document.getElementById("season-dt-stars").textContent = "★".repeat(carrera.estrellas) + "☆".repeat(5 - carrera.estrellas);
        document.getElementById("season-club-name").textContent = carrera.equipoActual?.nombre?.toUpperCase() || "SIN EQUIPO";
        document.getElementById("season-club-apodo").textContent = carrera.equipoActual?.apodo?.toUpperCase() || "";
        document.getElementById("season-club-badge").src = carrera.equipoActual?.rutaEscudo ? `../assets/${carrera.equipoActual.rutaEscudo}` : "../assets/logo-futbolmemoria.png";
    }

    function renderTabla(rows, equipoId) {
        const body = document.getElementById("season-table-body"); body.replaceChildren();
        rows.forEach((row) => {
            const tr = document.createElement("tr");
            if (row.posicion <= 6) tr.classList.add("zone-lib");
            else if (row.posicion <= 12) tr.classList.add("zone-sud");
            if (row.posicion > rows.length - 6) tr.classList.add("zone-desc");
            if (row.equipo.equipoId === equipoId) tr.classList.add("my-club");
            const team = row.equipo;
            const cells = [row.posicion, team.nombre, row.jugados, row.ganados, row.empatados, row.perdidos,
                row.golesFavor, row.golesContra, row.diferencia, row.puntos];
            cells.forEach((value, index) => {
                const td = document.createElement("td");
                if (index === 1) {
                    if (team.rutaEscudo) { const img = document.createElement("img"); img.src = `../assets/${team.rutaEscudo}`; img.alt = ""; td.appendChild(img); }
                    td.append(document.createTextNode(value));
                } else td.textContent = value;
                tr.appendChild(td);
            });
            body.appendChild(tr);
        });
    }
});
