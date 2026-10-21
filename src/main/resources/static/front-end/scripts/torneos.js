document.addEventListener("DOMContentLoaded", () => {
    const storage = window.FDM.storage;
    const dt = storage.getDT();
    const club = storage.getClub();
    const bracket = document.getElementById("tour-bracket");
    const select = document.getElementById("tour-select");
    const teams = document.getElementById("tour-bracket-teams");
    const bracketTitle = document.getElementById("tour-bracket-title");
    const bracketLogo = document.getElementById("tour-bracket-logo");
    const bracketCup = document.getElementById("tour-bracket-cup");
    const bracketRound = document.getElementById("tour-bracket-round");
    const pageTitle = document.querySelector(".tour-page-title");

    const torneosConfig = {
        libertadores: {
            titulo: "LIBERTADORES",
            logo: "../assets/logo-libertadores.png",
            copa: "../assets/copa-libertadores.png",
            ronda: "Octavos de Final"
        },
        sudamericana: {
            titulo: "SUDAMERICANA",
            logo: "../assets/logo-sudamericana.png",
            copa: "../assets/copa-sudamericana.png",
            ronda: "Octavos de Final"
        },
        nacional: {
            titulo: "NACIONAL",
            logo: "../assets/logo-nacional.png",
            copa: "../assets/copa-nacional.png",
            ronda: "Octavos de Final"
        }
    };

    document.getElementById("tour-dt-avatar").src = dt.avatarSrc || "../assets/avatar-1.png";
    document.getElementById("tour-dt-name").textContent = `${dt.nombre || "DT"} ${dt.apellido || ""}`.trim().toUpperCase();
    document.getElementById("tour-dt-stars").textContent = "★".repeat(dt.estrellas || 1) + "☆".repeat(5 - (dt.estrellas || 1));
    document.getElementById("tour-club-name").textContent = (club.name || "CLUB").toUpperCase();
    document.getElementById("tour-club-badge").src = club.escudo || "../assets/Boca_escudo.png";

    actualizarAccesos();

    document.querySelectorAll(".tour-big-btn").forEach((button) => {
        button.addEventListener("click", () => {
            if (button.classList.contains("locked")) return;
            abrirLlave(button.dataset.torneo);
        });
    });

    document.getElementById("btn-tour-menu").addEventListener("click", () => {
        if (!bracket.classList.contains("hidden")) {
            volverASeleccion();
            return;
        }
        window.location.href = "menu.html";
    });

    document.getElementById("btn-tour-play").addEventListener("click", () => {
        window.location.href = "partido.html";
    });

    teams.addEventListener("scroll", actualizarEncabezadosFixture, { passive: true });

    function actualizarAccesos() {
        const posicion = posicionEnTemporada(club.id);
        const btnLibertadores = document.querySelector('[data-torneo="libertadores"]');

        if (!btnLibertadores) return;

        if (posicion > 0 && posicion <= 6) {
            btnLibertadores.classList.remove("locked");
            btnLibertadores.querySelector("img")?.remove();
        }
    }

    function posicionEnTemporada(clubId) {
        const tabla = obtenerTablaTemporada();
        const index = tabla.findIndex((item) => item.id === clubId);
        return index >= 0 ? index + 1 : -1;
    }

    function obtenerTablaTemporada() {
        let temporada = storage.read(storage.KEYS.temporada, null);

        if (!temporada) {
            temporada = generarTemporadaDefault(club);
            storage.write(storage.KEYS.temporada, temporada);
        }

        return temporada.tabla;
    }

    function generarTemporadaDefault(clubActual) {
        const tabla = window.FDM.clubes.argentinos.map((item) => {
            const pj = item.id === clubActual.id ? 0 : Math.floor(Math.random() * 8) + 3;
            const pg = Math.floor(Math.random() * Math.max(1, pj));
            const pe = Math.floor(Math.random() * Math.max(1, pj - pg));
            const pp = Math.max(0, pj - pg - pe);
            const gf = pg * 2 + Math.floor(Math.random() * 8);
            const gc = pp * 2 + Math.floor(Math.random() * 8);

            return {
                ...item,
                pj,
                pg,
                pe,
                pp,
                gf,
                gc,
                dg: gf - gc,
                pts: pg * 3 + pe
            };
        }).sort((a, b) => b.pts - a.pts || b.dg - a.dg || b.gf - a.gf);

        return { fecha: 1, tabla };
    }

    function clubDesdeTabla(fila) {
        return window.FDM.clubes.byId(fila.id);
    }

    function armarParticipantes(torneo) {
        const tabla = obtenerTablaTemporada();
        const { internacionales } = window.FDM.clubes;

        if (torneo === "nacional") {
            return tabla.slice(0, 16).map(clubDesdeTabla);
        }

        if (torneo === "libertadores") {
            const argentinos = tabla.slice(0, 6).map(clubDesdeTabla);
            return mezclarLlave([...argentinos, ...internacionales.libertadores]).slice(0, 16);
        }

        if (torneo === "sudamericana") {
            const argentinos = tabla.slice(6, 12).map(clubDesdeTabla);
            return mezclarLlave([...argentinos, ...internacionales.sudamericana]).slice(0, 16);
        }

        return [];
    }

    function mezclarLlave(lista) {
        const copia = [...lista];

        for (let i = copia.length - 1; i > 0; i -= 1) {
            const j = Math.floor(Math.random() * (i + 1));
            [copia[i], copia[j]] = [copia[j], copia[i]];
        }

        return copia;
    }

    function crearEquipo(item) {
        if (!item || item.pending) {
            return `
                <span class="tour-team tour-team--pending">
                    <span class="tour-team-badge">?</span>
                    <strong class="tour-team-code">${item?.codigo || "POR DEFINIR"}</strong>
                </span>
            `;
        }

        return `
            <span class="tour-team">
                <span class="tour-team-badge" aria-hidden="true">
                    <img src="${item.escudo}" alt="">
                </span>
                <strong class="tour-team-code">${item.codigo || codigo(item.name)}</strong>
            </span>
        `;
    }

    function crearPartido(local, visitante, numero) {
        const match = document.createElement("article");
        match.className = "tour-match";
        match.innerHTML = `
            ${crearEquipo(local)}
            <span class="tour-vs">VS</span>
            ${crearEquipo(visitante)}
            <span class="tour-match-label">P${numero}</span>
        `;
        return match;
    }

    function crearRonda(nombre, partidos, claseExtra = "") {
        const column = document.createElement("section");
        column.className = `tour-round ${claseExtra}`.trim();
        column.innerHTML = `<h3>${nombre}</h3>`;
        partidos.forEach((partido) => column.appendChild(partido));
        return column;
    }

    function crearPlaceholder(texto) {
        return {
            name: texto,
            codigo: texto,
            escudo: "",
            pending: true
        };
    }

    function armarLlave(participantes) {
        const octavosIzquierda = [];
        const octavosDerecha = [];

        for (let i = 0; i < 4; i += 1) {
            octavosIzquierda.push(crearPartido(participantes[i * 2], participantes[i * 2 + 1], i + 1));
        }

        for (let i = 4; i < 8; i += 1) {
            octavosDerecha.push(crearPartido(participantes[i * 2], participantes[i * 2 + 1], i + 1));
        }

        const cuartosIzquierda = [
            crearPartido(crearPlaceholder("G1"), crearPlaceholder("G2"), 9),
            crearPartido(crearPlaceholder("G3"), crearPlaceholder("G4"), 10)
        ];
        const cuartosDerecha = [
            crearPartido(crearPlaceholder("G5"), crearPlaceholder("G6"), 11),
            crearPartido(crearPlaceholder("G7"), crearPlaceholder("G8"), 12)
        ];
        const semiIzquierda = [
            crearPartido(crearPlaceholder("G9"), crearPlaceholder("G10"), 13)
        ];
        const semiDerecha = [
            crearPartido(crearPlaceholder("G11"), crearPlaceholder("G12"), 14)
        ];
        const final = [
            crearPartido(crearPlaceholder("G13"), crearPlaceholder("G14"), 15)
        ];

        teams.appendChild(crearRonda("OCTAVOS", octavosIzquierda, "tour-round--octavos"));
        teams.appendChild(crearRonda("CUARTOS", cuartosIzquierda, "tour-round--cuartos"));
        teams.appendChild(crearRonda("SEMIFINAL", semiIzquierda, "tour-round--semis"));
        teams.appendChild(crearRonda("FINAL", final, "tour-round--final"));
        teams.appendChild(crearRonda("SEMIFINAL", semiDerecha, "tour-round--semis"));
        teams.appendChild(crearRonda("CUARTOS", cuartosDerecha, "tour-round--cuartos"));
        teams.appendChild(crearRonda("OCTAVOS", octavosDerecha, "tour-round--octavos"));
    }

    function abrirLlave(torneo) {
        const config = torneosConfig[torneo];
        const participantes = armarParticipantes(torneo);

        storage.write(storage.KEYS.torneos, {
            activo: torneo,
            ronda: config.ronda,
            participantes
        });

        select.classList.add("hidden");
        bracket.classList.remove("hidden");
        if (pageTitle) pageTitle.classList.add("hidden");
        bracketTitle.textContent = config.titulo;
        if (bracketRound) bracketRound.textContent = "OCTAVOS A FINAL";
        bracketLogo.src = config.logo;
        bracketLogo.alt = config.titulo;
        bracketCup.src = config.copa;
        bracketCup.alt = `Copa ${config.titulo}`;
        teams.innerHTML = "";
        teams.scrollTop = 0;
        actualizarEncabezadosFixture();
        armarLlave(participantes);
    }

    function volverASeleccion() {
        bracket.classList.add("hidden");
        select.classList.remove("hidden");
        if (pageTitle) pageTitle.classList.remove("hidden");
        teams.innerHTML = "";
        teams.classList.remove("is-scrolled");
    }

    function codigo(nombre) {
        return nombre.slice(0, 3).toUpperCase();
    }

    function actualizarEncabezadosFixture() {
        teams.classList.toggle("is-scrolled", teams.scrollTop > 2);
    }
});
