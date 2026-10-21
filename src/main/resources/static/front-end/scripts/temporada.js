document.addEventListener("DOMContentLoaded", () => {
    const storage = window.FDM.storage;
    const dt = storage.getDT();
    const club = storage.getClub();
    let temporada = storage.read(storage.KEYS.temporada, null);

    if (!temporada) {
        temporada = generarTemporada(club);
        storage.write(storage.KEYS.temporada, temporada);
    }

    document.getElementById("season-dt-avatar").src = dt.avatarSrc || "../assets/avatar-1.png";
    document.getElementById("season-dt-name").textContent = `${dt.nombre || "DT"} ${dt.apellido || ""}`.trim().toUpperCase();
    document.getElementById("season-dt-age").textContent = `EDAD: ${dt.edad || "--"}`;
    document.getElementById("season-dt-province").textContent = `PROVINCIA: ${dt.provincia || "--"}`;
    document.getElementById("season-dt-stars").textContent = "★".repeat(dt.estrellas || 1) + "☆".repeat(5 - (dt.estrellas || 1));
    document.getElementById("season-club-name").textContent = (club.name || "CLUB").toUpperCase();
    document.getElementById("season-club-apodo").textContent = (club.apodo || "APODO").toUpperCase();
    document.getElementById("season-club-badge").src = club.escudo || "../assets/Boca_escudo.png";

    renderTabla(temporada.tabla);

    document.getElementById("btn-season-menu").addEventListener("click", () => {
        window.location.href = "menu.html";
    });

    function generarTemporada(clubActual) {
        const tabla = window.FDM.clubes.all.map((item) => {
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

    function renderTabla(tabla) {
        const body = document.getElementById("season-table-body");
        body.innerHTML = "";

        tabla.forEach((item, index) => {
            const row = document.createElement("tr");
            if (index < 6) row.className = "zone-lib";
            if (index >= 6 && index < 12) row.className = "zone-sud";
            if (index >= 24 && index < 30) row.className = "zone-desc";
            if (item.id === club.id) row.classList.add("my-club");

            row.innerHTML = `
                <td>${index + 1}</td>
                <td><img src="${item.escudo}" alt="">${item.name}</td>
                <td>${item.pj}</td><td>${item.pg}</td><td>${item.pe}</td><td>${item.pp}</td>
                <td>${item.gf}</td><td>${item.gc}</td><td>${item.dg}</td><td>${item.pts}</td>
            `;
            body.appendChild(row);
        });
    }
});
