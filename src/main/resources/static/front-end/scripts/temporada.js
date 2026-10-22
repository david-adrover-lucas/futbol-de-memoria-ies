document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session) return;
    const api = window.FDM.services;
    const title = document.querySelector(".screen-title-plate");
    const tableWrap = document.querySelector(".season-table-wrap");
    const calendarInfo = document.getElementById("season-calendar-info");
    let calendar;
    let carrera;

    try {
        [carrera, calendar] = await Promise.all([
            api.carreraService.obtener(session.usuarioId), api.calendarioService.actual(session.usuarioId)
        ]);
        renderHeader(carrera);
        renderCalendar(calendar);
        if (calendar.codigoCompeticionActual === "temporada") {
            const temporada = await api.temporadaService.obtener(session.usuarioId);
            renderTabla(temporada.tabla, carrera.equipoActual?.equipoId);
        } else {
            tableWrap.classList.add("hidden");
        }
    } catch (error) { alert(error.message); return; }

    document.getElementById("btn-season-menu").addEventListener("click", () => window.location.assign("menu.html"));
    function renderCalendar(data) {
        const competition = data.nombreCompeticionActual || data.codigoCompeticionActual;
        const round = data.nombreRondaActual ? ` · ${data.nombreRondaActual}` : "";
        title.textContent = `TEMPORADA ${data.numeroTemporada} · FECHA ${data.fechaActual}/${data.totalFechas}`;
        calendarInfo.textContent = `${competition}${round}${data.esFechaLibre ? " · Fecha libre: se simularán los partidos de CPU" : " · Juega tu partido"}`;
    }

    function renderHeader(data) {
        document.getElementById("season-dt-avatar").src = `../assets/avatar-${data.numeroAvatar || 1}.png`;
        document.getElementById("season-dt-name").textContent = `${data.nombreDt} ${data.apellidoDt}`.toUpperCase();
        document.getElementById("season-dt-age").textContent = `EDAD: ${data.edadDt}`;
        document.getElementById("season-dt-province").textContent = `PROVINCIA: ${data.provincia}`;
        document.getElementById("season-dt-stars").textContent = "★".repeat(data.estrellas) + "☆".repeat(5 - data.estrellas);
        document.getElementById("season-club-name").textContent = data.equipoActual?.nombre?.toUpperCase() || "SIN EQUIPO";
        document.getElementById("season-club-apodo").textContent = data.equipoActual?.apodo?.toUpperCase() || "";
        document.getElementById("season-club-badge").src = data.equipoActual?.rutaEscudo ? `../assets/${data.equipoActual.rutaEscudo}` : "../assets/logo-futbolmemoria.png";
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
