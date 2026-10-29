document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session) return;
    const api = window.FDM.services;
    const title = document.querySelector(".placa-titulo-pantalla");
    const tableWrap = document.querySelector(".contenedor-tabla-temporada");
    const informacionCalendario = document.getElementById("temporada-informacion-calendario");
    let calendar;
    let carrera;

    try {
        [carrera, calendar] = await Promise.all([
            api.carreraService.obtener(session.usuarioId), api.calendarioService.actual(session.usuarioId)
        ]);
        renderHeader(carrera);
        renderizarCalendario(calendar);
        if (calendar.codigoCompeticionActual === "temporada") {
            const temporada = await api.temporadaService.obtener(session.usuarioId);
            renderTabla(temporada.tabla, carrera.equipoActual?.equipoId);
        } else {
            tableWrap.classList.add("oculto");
        }
    } catch (error) { alert(error.message); return; }

    document.getElementById("boton-menu-temporada").addEventListener("click", () => window.location.assign("menu.html"));
    function renderizarCalendario(data) {
        const competition = data.nombreCompeticionActual || data.codigoCompeticionActual;
        const round = data.nombreRondaActual ? ` · ${data.nombreRondaActual}` : "";
        title.textContent = `TEMPORADA ${data.numeroTemporada} · FECHA ${data.fechaActual}/${data.totalFechas}`;
        informacionCalendario.textContent = `${competition}${round}${data.esFechaLibre ? " · Fecha libre: se simularán los partidos de CPU" : " · Juega tu partido"}`;
    }

    function renderHeader(data) {
        document.getElementById("temporada-avatar-entrenador").src = `../assets/avatar-${data.numeroAvatar || 1}.png`;
        document.getElementById("temporada-nombre-entrenador").textContent = `${data.nombreDt} ${data.apellidoDt}`.toUpperCase();
        document.getElementById("temporada-edad-entrenador").textContent = `EDAD: ${data.edadDt}`;
        document.getElementById("temporada-provincia-entrenador").textContent = `PROVINCIA: ${data.provincia}`;
        document.getElementById("temporada-estrellas-entrenador").textContent = "★".repeat(data.estrellas) + "☆".repeat(5 - data.estrellas);
        document.getElementById("temporada-nombre-club").textContent = data.equipoActual?.nombre?.toUpperCase() || "SIN EQUIPO";
        document.getElementById("temporada-apodo-club").textContent = data.equipoActual?.apodo?.toUpperCase() || "";
        document.getElementById("temporada-insignia-club").src = data.equipoActual?.rutaEscudo ? `../assets/${data.equipoActual.rutaEscudo}` : "../assets/logo-futbolmemoria.png";
    }

    function renderTabla(rows, equipoId) {
        const body = document.getElementById("cuerpo-tabla-temporada"); body.replaceChildren();
        rows.forEach((row) => {
            const tr = document.createElement("tr");
            if (row.posicion <= 6) tr.classList.add("zona-libertadores");
            else if (row.posicion <= 12) tr.classList.add("zona-sudamericana");
            if (row.posicion > rows.length - 6) tr.classList.add("zona-descenso");
            if (row.equipo.equipoId === equipoId) tr.classList.add("mi-club");
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
