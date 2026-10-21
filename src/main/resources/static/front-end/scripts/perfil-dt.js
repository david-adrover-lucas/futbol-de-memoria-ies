document.addEventListener("DOMContentLoaded", () => {
    const storage = window.FDM.storage;
    const dt = storage.getDT();
    const club = storage.getClub();
    const carrera = storage.getCarrera();
    const progreso = window.FDM.experiencia.progreso(dt);
    const noticias = storage.read(storage.KEYS.noticias, []);
    const mensajes = storage.read(storage.KEYS.mensajes, []);

    const paneles = {
        historial: {
            titulo: "HISTORIAL DE CLUBES",
            filas: carrera.historial.length
                ? carrera.historial.map((item) => [item.club, item.temporadas])
                : [[club.name, "Temporada actual"]]
        },
        titulos: {
            titulo: "TITULOS",
            filas: carrera.trofeos.length
                ? carrera.trofeos.map((item) => [item.torneo, item.club])
                : [["Sin titulos oficiales", "En disputa"]]
        },
        trofeos: {
            titulo: "TROFEOS",
            filas: carrera.trofeos.length
                ? carrera.trofeos.map((item) => [item.torneo, item.fecha])
                : [["Vitrina", "A la espera del primer trofeo"]]
        },
        logros: {
            titulo: "LOGROS",
            filas: [
                ["Partidos jugados", carrera.estadisticas.jugados],
                ["Victorias", carrera.estadisticas.ganados],
                ["Goles a favor", carrera.estadisticas.gf],
                ["Goles en contra", carrera.estadisticas.gc]
            ]
        },
        ofertas: {
            titulo: "OFERTAS",
            filas: window.FDM.clubes.all
                .filter((item) => item.stars <= Math.min(5, (dt.estrellas || 1) + 1))
                .slice(0, 5)
                .map((item) => [item.name, `${item.stars} estrellas - $ ${item.sueldo}`])
        },
        noticias: {
            titulo: "NOTICIAS",
            filas: noticias.length
                ? noticias.map((item) => [item.titulo, item.texto])
                : [["Mercado tranquilo", "No hay noticias destacadas."]]
        },
        mensajes: {
            titulo: "MENSAJES",
            filas: mensajes.length
                ? mensajes.map((item) => [item.de, item.texto])
                : [["Presidencia", `Bienvenido a ${club.name}.`]]
        }
    };

    document.getElementById("profile-name").textContent = `${dt.nombre || "DT"} ${dt.apellido || ""}`.trim().toUpperCase();
    document.getElementById("profile-stars").textContent = "★".repeat(dt.estrellas || 1) + "☆".repeat(5 - (dt.estrellas || 1));
    document.getElementById("profile-exp-bar").style.width = `${progreso.porcentaje}%`;
    document.getElementById("profile-exp-text").textContent = `${progreso.resto}/2000`;
    document.getElementById("profile-meta").innerHTML = `${dt.provincia || "--"}<br>${dt.edad || "--"} ANOS`;
    document.getElementById("profile-avatar").src = dt.avatarSrc || "../assets/avatar-1.png";

    document.querySelectorAll(".profile-btn").forEach((button) => {
        button.addEventListener("click", () => abrir(button.dataset.panel));
    });

    document.getElementById("btn-profile-menu").addEventListener("click", () => {
        window.location.href = "menu.html";
    });

    const initial = new URLSearchParams(window.location.search).get("tab") || "historial";
    abrir(paneles[initial] ? initial : "historial");

    function abrir(id) {
        const data = paneles[id];
        document.querySelectorAll(".profile-btn").forEach((button) => {
            button.classList.toggle("active", button.dataset.panel === id);
        });
        document.getElementById("profile-panel-title").textContent = data.titulo;
        const body = document.getElementById("profile-panel-body");
        body.innerHTML = "";

        data.filas.forEach(([label, value]) => {
            const row = document.createElement("div");
            row.className = "profile-row";
            row.innerHTML = `<span>${label}</span><strong>${value}</strong>`;
            body.appendChild(row);
        });
    }
});
