document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session) return;
    const api = window.FDM.services;
    const body = document.getElementById("profile-panel-body");
    const title = document.getElementById("profile-panel-title");
    let carrera;
    try { carrera = await api.carreraService.obtener(session.usuarioId); }
    catch (error) { alert(error.message); return; }
    renderHeader(carrera);

    document.querySelectorAll(".profile-btn").forEach((button) => button.addEventListener("click", async () => {
        document.querySelectorAll(".profile-btn").forEach((item) => item.classList.remove("active"));
        button.classList.add("active");
        title.textContent = button.textContent.trim();
        body.replaceChildren();
        try { await renderPanel(button.dataset.panel); }
        catch (error) { showMessage(error.message); }
    }));
    document.getElementById("btn-profile-menu").addEventListener("click", () => window.location.assign("menu.html"));
    await renderPanel("historial");

    async function renderPanel(key) {
        if (key === "historial") {
            const matches = await api.estadisticaService.historial(session.usuarioId);
            matches.filter((game) => game.estado === "finalizado").forEach((game) => {
                const result = game.golesLocal > game.golesVisitante ? "Victoria" : game.golesLocal < game.golesVisitante ? "Derrota" : "Empate";
                showRow(`${game.competicion?.nombre || "Partido"} · ${fecha(game.finalizadoEn)}`,
                    `${game.equipoLocal?.nombre || "Local"} ${game.golesLocal} - ${game.golesVisitante} ${game.equipoVisitante?.nombre || "Visitante"} · ${result}`);
            });
            if (!matches.some((game) => game.estado === "finalizado")) showMessage("Todavía no hay partidos finalizados.");
        } else if (key === "ofertas") {
            const teams = await api.equipoService.seleccionables(carrera.estrellas || 1);
            const candidates = teams.filter((team) => team.equipoId !== carrera.equipoActual?.equipoId);
            if (!candidates.length) return showMessage("No hay equipos disponibles para tu nivel actual.");
            candidates.forEach((team) => {
                const row = document.createElement("div"); row.className = "profile-info-row";
                const label = document.createElement("strong"); label.textContent = `${team.nombre} · ${team.prestigio} estrellas`;
                const change = document.createElement("button"); change.type = "button"; change.textContent = "CAMBIAR EQUIPO";
                change.addEventListener("click", async () => {
                    change.disabled = true;
                    try { carrera = await api.carreraService.cambiarEquipo(carrera.carreraId, team.equipoId); renderHeader(carrera); showMessage(`Ahora dirigís a ${team.nombre}.`); }
                    catch (error) { alert(error.message); change.disabled = false; }
                });
                row.append(label, change); body.appendChild(row);
            });
        } else {
            const messages = {
                titulos: "Los títulos todavía no tienen persistencia en el backend.",
                trofeos: "Los trofeos todavía no tienen persistencia en el backend.",
                logros: "Los logros todavía no tienen persistencia en el backend.",
                noticias: "Las noticias todavía no tienen persistencia en el backend.",
                mensajes: "Los mensajes todavía no tienen persistencia en el backend."
            };
            showMessage(messages[key] || "No hay datos para mostrar.");
        }
    }

    function renderHeader(data) {
        document.getElementById("profile-name").textContent = `${data.nombreDt} ${data.apellidoDt}`.toUpperCase();
        document.getElementById("profile-meta").innerHTML = `${escapeHtml(data.provincia)}<br>${data.edadDt} AÑOS`;
        document.getElementById("profile-avatar").src = `../assets/avatar-${data.numeroAvatar || 1}.png`;
        const stars = Number(data.estrellas) || 1;
        document.getElementById("profile-stars").textContent = "★".repeat(stars) + "☆".repeat(5 - stars);
        const exp = Number(data.experiencia) || 0;
        const floor = [0, 0, 500, 1500, 3000, 5000][stars];
        const next = [500, 500, 1500, 3000, 5000, 5000][stars];
        document.getElementById("profile-exp-bar").style.width = `${Math.min(100, ((exp - floor) / Math.max(1, next - floor)) * 100)}%`;
        document.getElementById("profile-exp-text").textContent = `${exp} XP`;
    }
    function showRow(label, value) {
        const row = document.createElement("div"); row.className = "profile-info-row";
        const left = document.createElement("span"); left.textContent = label;
        const right = document.createElement("strong"); right.textContent = value;
        row.append(left, right); body.appendChild(row);
    }
    function showMessage(message) { const p = document.createElement("p"); p.textContent = message; body.appendChild(p); }
    function fecha(value) { return value ? new Date(value).toLocaleString("es-AR") : ""; }
    function escapeHtml(value) { return String(value || "").replace(/[&<>"']/g, (char) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[char])); }
});
