(function () {
    function expParaResultado(resultado) {
        if (resultado === "victoria") return 100;
        if (resultado === "empate") return 50;
        return -50;
    }

    function aplicar(dt, delta) {
        const actual = Number(dt.experiencia) || 0;
        const total = Math.max(0, actual + delta);
        const estrellas = Math.max(1, Math.min(5, Math.floor(total / 2000) + 1));
        return { ...dt, experiencia: total, estrellas };
    }

    function progreso(dt) {
        const exp = Number(dt.experiencia) || 0;
        return {
            actual: exp,
            nivel: Math.floor(exp / 2000) + 1,
            resto: exp % 2000,
            porcentaje: Math.min(100, ((exp % 2000) / 2000) * 100)
        };
    }

    window.FDM = window.FDM || {};
    window.FDM.experiencia = { expParaResultado, aplicar, progreso };
})();
