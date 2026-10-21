document.addEventListener("DOMContentLoaded", () => {
    const storage = window.FDM.storage;
    const partido = storage.read(storage.KEYS.ultimoPartido, null);

    if (!partido) {
        window.location.href = "menu.html";
        return;
    }

    const resultado = window.FDM.enginePartido.resultado(partido);
    const deltaExp = window.FDM.experiencia.expParaResultado(resultado);
    const dtActualizado = window.FDM.experiencia.aplicar(storage.getDT(), deltaExp);
    const progreso = window.FDM.experiencia.progreso(dtActualizado);
    const carrera = storage.getCarrera();

    carrera.estadisticas.jugados += 1;
    carrera.estadisticas.gf += partido.golesLocal;
    carrera.estadisticas.gc += partido.golesRival;
    if (resultado === "victoria") carrera.estadisticas.ganados += 1;
    if (resultado === "empate") carrera.estadisticas.empatados += 1;
    if (resultado === "derrota") carrera.estadisticas.perdidos += 1;

    storage.saveDT(dtActualizado);
    storage.saveCarrera(carrera);
    storage.pushList(storage.KEYS.noticias, {
        fecha: new Date().toISOString(),
        titulo: `${partido.club.name} ${partido.golesLocal} - ${partido.golesRival} ${partido.rival.name}`,
        texto: noticia(resultado, partido)
    }, 12);
    storage.pushList(storage.KEYS.mensajes, {
        de: "Presidencia",
        texto: mensaje(resultado, partido)
    }, 12);

    document.body.classList.add(`result-${resultado}`);
    document.getElementById("result-banner").src = resultado === "derrota" ? "../assets/cartel-perdiste.png" : "../assets/cartel-ganaste.png";
    document.getElementById("result-banner-text").textContent = textoResultado(resultado);
    document.getElementById("result-exp-img").src = deltaExp < 0 ? "../assets/exp-negativa.png" : "../assets/exp-positiva.png";
    document.getElementById("result-exp-delta").textContent = `${deltaExp > 0 ? "+" : ""}${deltaExp} XP`;
    document.getElementById("result-local-img").src = partido.club.escudo;
    document.getElementById("result-away-img").src = partido.rival.escudo;
    const posesion = calcularPosesion(partido);
    document.getElementById("result-local-badge").textContent = `${posesion.local}%`;
    document.getElementById("result-away-badge").textContent = `${posesion.rival}%`;
    document.getElementById("result-shots-local").textContent = partido.golesLocal;
    document.getElementById("result-shots-away").textContent = partido.golesRival;
    document.getElementById("result-wild-local").textContent = 3 - Object.values(partido.comodines).reduce((sum, value) => sum + value, 0);
    document.getElementById("result-wild-away").textContent = Math.max(0, partido.rival.stars - 2);
    document.getElementById("result-stars").textContent = "★".repeat(dtActualizado.estrellas) + "☆".repeat(5 - dtActualizado.estrellas);
    document.getElementById("result-exp-bar").style.width = `${progreso.porcentaje}%`;
    document.getElementById("result-exp-text").textContent = `${progreso.resto}/2000`;

    document.getElementById("btn-back-menu-result").addEventListener("click", () => {
        window.location.href = "menu.html";
    });

    function noticia(tipo, game) {
        if (tipo === "victoria") return `${game.club.name} consiguio una victoria clave ante ${game.rival.name}.`;
        if (tipo === "empate") return `${game.club.name} y ${game.rival.name} repartieron puntos en un partido cerrado.`;
        return `${game.club.name} cayo ante ${game.rival.name} y debera reaccionar.`;
    }

    function mensaje(tipo, game) {
        if (tipo === "victoria") return `Buen trabajo. La victoria contra ${game.rival.name} fortalece el proyecto.`;
        if (tipo === "empate") return "Sumamos, pero necesitamos mas decision en los momentos importantes.";
        return "El resultado preocupa. La proxima fecha necesitamos una respuesta.";
    }

    function textoResultado(tipo) {
        if (tipo === "victoria") return "GANASTE";
        if (tipo === "derrota") return "PERDISTE";
        return "EMPATE";
    }

    function calcularPosesion(game) {
        const localPrestigio = Number(game.club.stars) || 1;
        const rivalPrestigio = Number(game.rival.stars) || 1;
        const diferencia = Math.min(4, Math.abs(localPrestigio - rivalPrestigio));
        const ventaja = 4 + diferencia * 4;

        if (localPrestigio > rivalPrestigio) {
            return { local: 50 + ventaja, rival: 50 - ventaja };
        }

        if (rivalPrestigio > localPrestigio) {
            return { local: 50 - ventaja, rival: 50 + ventaja };
        }

        return { local: 50, rival: 50 };
    }
});
