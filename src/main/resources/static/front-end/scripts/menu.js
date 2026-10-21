document.addEventListener("DOMContentLoaded", () => {
    const storage = window.FDM.storage;
    const dt = storage.getDT();
    const club = storage.getClub();
    const carrera = storage.getCarrera();
    const progreso = window.FDM.experiencia.progreso(dt);

    const panelData = {
        estadisticas: {
            title: "ESTADISTICAS",
            rows: [
                ["Partidos jugados", carrera.estadisticas.jugados],
                ["Victorias", carrera.estadisticas.ganados],
                ["Empates", carrera.estadisticas.empatados],
                ["Derrotas", carrera.estadisticas.perdidos],
                ["Goles a favor", carrera.estadisticas.gf],
                ["Goles en contra", carrera.estadisticas.gc]
            ]
        },
        objetivo: {
            title: "OBJETIVO DEL CLUB",
            rows: [
                ["Club actual", club.name],
                ["Objetivo principal", carrera.contrato.objetivo],
                ["Estado", carrera.contrato.estado],
                ["Temporada", carrera.contrato.temporada]
            ]
        },
        ofertas: {
            title: "OFERTAS DE CLUBES",
            rows: generarOfertas(dt)
        },
        ranking: {
            title: "RANKING DE DT",
            rows: [
                ["#1", "Marcelo Gallardo - 5 estrellas"],
                ["#2", "Carlos Bianchi - 5 estrellas"],
                ["#3", "Lucas Ardrover - Proyecto FDM"],
                ["Tu posicion", `${Math.max(20, 180 - progreso.nivel * 18)}`]
            ]
        },
        mensajes: {
            title: "BANDEJA DE MENSAJES",
            rows: generarMensajes(club, carrera)
        }
    };

    document.getElementById("menu-dt-avatar").src = dt.avatarSrc || `../assets/avatar-${dt.avatar || 1}.png`;
    document.getElementById("menu-dt-name").textContent = `${dt.nombre || "DT"} ${dt.apellido || ""}`.trim().toUpperCase();
    document.getElementById("menu-dt-age").textContent = `${dt.edad || "--"} AÑOS`;
    document.getElementById("menu-dt-province").textContent = (dt.provincia || "--").toUpperCase();
    renderMenuStars(dt.estrellas || 1);
    document.getElementById("menu-exp-bar").style.width = `${progreso.porcentaje}%`;
    document.getElementById("menu-exp-label").textContent = `${progreso.resto}/2000`;
    document.getElementById("menu-club-name").textContent = (club.name || "CLUB").toUpperCase();
    document.getElementById("menu-club-apodo").textContent = (club.apodo || "APODO").toUpperCase();
    document.getElementById("menu-club-badge").src = club.escudo || "../assets/Boca_escudo.png";

    const panel = document.getElementById("menu-secondary-panel");
    const panelTitle = document.getElementById("menu-panel-title");
    const panelBody = document.getElementById("menu-panel-body");

    document.querySelectorAll(".menu-btn").forEach((button) => {
        button.addEventListener("click", () => {
            document.querySelectorAll(".menu-btn").forEach((item) => item.classList.remove("active"));
            button.classList.add("active");

            if (button.dataset.action) {
                navegar(button.dataset.action);
                return;
            }

            abrirPanel(button.dataset.panel);
        });
    });

    document.getElementById("btn-close-menu-panel").addEventListener("click", () => {
        panel.classList.add("hidden");
    });

    document.getElementById("btn-save-game").addEventListener("click", () => {
        storage.saveTimestamp();
        alert("Partida guardada.");
    });

    document.getElementById("btn-logout-menu").addEventListener("click", () => {
        window.location.href = "../index.html";
    });

    function navegar(action) {
        const routes = {
            jugar: "partido.html",
            torneos: "torneos.html",
            temporada: "temporada.html",
            noticias: "perfil-dt.html?tab=noticias"
        };

        if (routes[action]) window.location.href = routes[action];
    }

    function abrirPanel(panelId) {
        const data = panelData[panelId];
        if (!data) return;

        panelTitle.textContent = data.title;
        panelBody.innerHTML = "";

        data.rows.forEach(([label, value]) => {
            const row = document.createElement("div");
            row.className = "menu-info-row";
            row.innerHTML = `<span>${label}</span><strong>${value}</strong>`;
            panelBody.appendChild(row);
        });

        panel.classList.remove("hidden");
    }

    function renderMenuStars(estrellas) {
        const container = document.getElementById("menu-dt-stars");
        if (!container) return;

        const total = 5;
        const filled = Math.max(1, Math.min(total, Number(estrellas) || 1));
        container.innerHTML = "";

        for (let i = 1; i <= total; i += 1) {
            const star = document.createElement("span");
            star.className = `menu-star ${i <= filled ? "is-filled" : "is-empty"}`;
            star.textContent = "★";
            container.appendChild(star);
        }
    }

    function generarOfertas(dtActual) {
        const nivel = Math.max(1, Number(dtActual.estrellas) || 1);
        return window.FDM.clubes.all
            .filter((item) => item.stars <= Math.min(5, nivel + 1))
            .slice(0, 4)
            .map((item) => [item.name, `${item.stars} estrellas - $ ${item.sueldo}`]);
    }

    function generarMensajes(clubActual, carreraActual) {
        return [
            ["Presidencia", `Bienvenido a ${clubActual.name}.`],
            ["Ayudante", "El plantel esta listo para competir."],
            ["Prensa", `${clubActual.apodo} espera una temporada fuerte.`],
            ["Contrato", carreraActual.contrato.estado]
        ];
    }
});
