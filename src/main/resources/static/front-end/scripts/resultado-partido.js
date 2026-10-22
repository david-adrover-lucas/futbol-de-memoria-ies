document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session || !session.ultimoPartidoId) { window.location.replace("menu.html"); return; }
    const { partidoService, carreraService, estadisticaService } = window.FDM.services;
    try {
        const [game, carrera, estadisticas, comodines] = await Promise.all([
            partidoService.estado(session.usuarioId, session.ultimoPartidoId),
            carreraService.obtener(session.usuarioId),
            estadisticaService.obtener(session.usuarioId),
            partidoService.comodinesUsados(session.usuarioId, session.ultimoPartidoId)
        ]);
        const result = game.golesLocal > game.golesVisitante ? "victoria" : game.golesLocal < game.golesVisitante ? "derrota" : "empate";
        document.body.classList.add(`result-${result}`);
        const delta = Number(game.variacionExperiencia) || 0;
        const stars = Math.max(1, Number(carrera.estrellas) || 1);
        const levelFloor = [0, 0, 500, 1500, 3000, 5000][stars];
        const levelNext = [500, 500, 1500, 3000, 5000, 5000][stars];
        const progress = Math.min(100, Math.max(0, ((carrera.experiencia - levelFloor) / Math.max(1, levelNext - levelFloor)) * 100));

        document.getElementById("result-banner").src = result === "derrota" ? "../assets/cartel-perdiste.png" : "../assets/cartel-ganaste.png";
        document.getElementById("result-banner-text").textContent = result === "victoria" ? "GANASTE" : result === "derrota" ? "PERDISTE" : "EMPATE";
        document.getElementById("result-exp-img").src = delta < 0 ? "../assets/exp-negativa.png" : "../assets/exp-positiva.png";
        document.getElementById("result-exp-delta").textContent = `${delta > 0 ? "+" : ""}${delta} XP`;
        document.getElementById("result-local-img").src = rutaEscudo(game.equipoLocal);
        document.getElementById("result-away-img").src = rutaEscudo(game.equipoVisitante);
        document.getElementById("result-local-badge").textContent = game.respuestasCorrectas;
        document.getElementById("result-away-badge").textContent = game.respuestasIncorrectas;
        document.getElementById("result-shots-local").textContent = game.golesLocal;
        document.getElementById("result-shots-away").textContent = game.golesVisitante;
        document.getElementById("result-wild-local").textContent = comodines.length;
        document.getElementById("result-wild-away").textContent = "—";
        document.getElementById("result-stars").textContent = "★".repeat(stars) + "☆".repeat(5 - stars);
        document.getElementById("result-exp-bar").style.width = `${progress}%`;
        document.getElementById("result-exp-text").textContent = `${carrera.experiencia} XP`;
        document.getElementById("result-date").textContent = game.competicion?.nombre || "PARTIDO FINALIZADO";
        document.querySelector(".result-stats > div:first-child strong").textContent = "RESPUESTAS CORRECTAS / INCORRECTAS";
        document.querySelector(".result-stats > div:nth-child(2) strong").textContent = "GOLES DEL PARTIDO";
        document.querySelector(".result-stats > div:nth-child(3) strong").textContent = "COMODINES USADOS";
        window.FDM.session.set({ ...session, carreraId: carrera.carreraId, carrera, estadisticas });
    } catch (error) {
        alert(error.message);
        window.location.replace("menu.html");
        return;
    }
    document.getElementById("btn-back-menu-result").addEventListener("click", () => {
        const current = window.FDM.session.get() || {};
        const { ultimoPartidoId, ...clean } = current;
        window.FDM.session.set(clean);
        window.location.assign("menu.html");
    });

    function rutaEscudo(team) { return team?.rutaEscudo ? `../assets/${team.rutaEscudo}` : "../assets/logo-futbolmemoria.png"; }
});
