document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session || !session.ultimoPartidoId) { window.location.replace("menu.html"); return; }
    const { partidoService, tandaPenalesService, carreraService, estadisticaService } = window.FDM.services;
    try {
        const [game, carrera, estadisticas, comodines] = await Promise.all([
            partidoService.estado(session.usuarioId, session.ultimoPartidoId),
            carreraService.obtener(session.usuarioId),
            estadisticaService.obtener(session.usuarioId),
            partidoService.comodinesUsados(session.usuarioId, session.ultimoPartidoId)
        ]);
        let tanda = null;
        if (["libertadores", "sudamericana"].includes(game.competicion?.codigo)
            && game.golesLocal === game.golesVisitante) {
            try { tanda = await tandaPenalesService.obtener(session.usuarioId, game.partidoId); }
            catch (error) { if (error.status !== 400 && error.status !== 404) throw error; }
        }
        const result = tanda?.estado === "finalizada"
            ? (tanda.equipoGanadorId === game.equipoLocal?.equipoId ? "victoria" : "derrota")
            : game.golesLocal > game.golesVisitante ? "victoria" : game.golesLocal < game.golesVisitante ? "derrota" : "empate";
        document.body.classList.add(`resultado-${result}`);
        const delta = Number(game.variacionExperiencia) || 0;
        const stars = Math.max(1, Number(carrera.estrellas) || 1);
        const levelFloor = [0, 0, 500, 1500, 3000, 5000][stars];
        const levelNext = [500, 500, 1500, 3000, 5000, 5000][stars];
        const progress = Math.min(100, Math.max(0, ((carrera.experiencia - levelFloor) / Math.max(1, levelNext - levelFloor)) * 100));

        document.getElementById("resultado-cartel").src = result === "derrota" ? "../assets/cartel-perdiste.png" : "../assets/cartel-ganaste.png";
        document.getElementById("resultado-texto-cartel").textContent = result === "victoria" ? "GANASTE" : result === "derrota" ? "PERDISTE" : "EMPATE";
        document.getElementById("resultado-imagen-experiencia").src = delta < 0 ? "../assets/exp-negativa.png" : "../assets/exp-positiva.png";
        document.getElementById("resultado-variacion-experiencia").textContent = `${delta > 0 ? "+" : ""}${delta} XP`;
        document.getElementById("resultado-imagen-local").src = rutaEscudo(game.equipoLocal);
        document.getElementById("resultado-imagen-visitante").src = rutaEscudo(game.equipoVisitante);
        document.getElementById("resultado-insignia-local").textContent = game.respuestasCorrectas;
        document.getElementById("resultado-insignia-visitante").textContent = game.respuestasIncorrectas;
        document.getElementById("resultado-remates-local").textContent = game.golesLocal;
        document.getElementById("resultado-remates-visitante").textContent = game.golesVisitante;
        if (tanda?.estado === "finalizada") {
            const penales = document.getElementById("resultado-penales");
            penales.textContent = `PENALES ${tanda.golesJugador} - ${tanda.golesRival}`;
            penales.classList.remove("oculto");
        }
        document.getElementById("resultado-comodines-local").textContent = comodines.length;
        document.getElementById("resultado-comodines-visitante").textContent = "—";
        document.getElementById("resultado-estrellas").textContent = "★".repeat(stars) + "☆".repeat(5 - stars);
        document.getElementById("resultado-barra-experiencia").style.width = `${progress}%`;
        document.getElementById("resultado-texto-experiencia").textContent = `${carrera.experiencia} XP`;
        document.getElementById("resultado-fecha").textContent = game.competicion?.nombre || "PARTIDO FINALIZADO";
        document.querySelector(".resultado-estadisticas > div:first-child strong").textContent = "RESPUESTAS CORRECTAS / INCORRECTAS";
        document.querySelector(".resultado-estadisticas > div:nth-child(2) strong").textContent = "GOLES DEL PARTIDO";
        document.querySelector(".resultado-estadisticas > div:nth-child(3) strong").textContent = "COMODINES USADOS";
        window.FDM.session.set({ ...session, carreraId: carrera.carreraId, carrera, estadisticas });
    } catch (error) {
        alert(error.message);
        window.location.replace("menu.html");
        return;
    }
    document.getElementById("boton-volver-menu-resultado").addEventListener("click", () => {
        const current = window.FDM.session.get() || {};
        const { ultimoPartidoId, ...clean } = current;
        window.FDM.session.set(clean);
        window.location.assign("menu.html");
    });

    function rutaEscudo(team) { return team?.rutaEscudo ? `../assets/${team.rutaEscudo}` : "../assets/logo-futbolmemoria.png"; }
});
