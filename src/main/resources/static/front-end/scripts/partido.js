document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session) return;
    const api = window.FDM.services.partidoService;
    const penalesApi = window.FDM.services.tandaPenalesService;
    const params = new URLSearchParams(window.location.search);
    const competition = params.get("competicion") || session.competicionCodigo || "temporada";
    const ui = {
        local: document.getElementById("score-local-goals"), away: document.getElementById("score-away-goals"),
        localCode: document.getElementById("score-local-code"), awayCode: document.getElementById("score-away-code"),
        period: document.getElementById("score-period"), cup: document.getElementById("score-cup"), competition: document.getElementById("score-competition"),
        question: document.getElementById("question-text"), answers: document.getElementById("answers-grid"), timer: document.getElementById("question-timer")
    };
    let game = null;
    let question = null;
    let options = [];
    let interval = null;
    let startedAt = 0;
    let seconds = 15;
    let busy = false;

    try {
        if (session.partidoActualId) {
            try { game = await api.estado(session.usuarioId, session.partidoActualId); }
            catch (error) { if (error.status !== 404) throw error; }
        }
        if (game && game.estado === "finalizado" && esEliminatoriaEmpatada(game)) {
            await openPenalties(game);
            return;
        }
        if (!game || game.estado !== "en_curso") {
            game = await api.iniciar(session.usuarioId, competition);
            window.FDM.session.set({ ...session, partidoActualId: game.partidoId, competicionCodigo: competition, comodinesUsados: [] });
        }
        await loadQuestion();
    } catch (error) { alert(error.message); window.location.assign("menu.html"); return; }

    document.querySelectorAll(".wildcard-btn[data-wildcard]").forEach((button) => button.addEventListener("click", async () => {
        if (busy || !question) return;
        busy = true;
        try {
            await api.comodin(session.usuarioId, game.partidoId, button.dataset.wildcard);
            await loadQuestion();
        } catch (error) { alert(error.message); }
        finally { busy = false; }
    }));
    document.getElementById("btn-exit-match").addEventListener("click", async () => {
        if (!confirm("¿Finalizar y guardar este partido?")) return;
        stopTimer();
        try {
            game = await api.finalizar(session.usuarioId, game.partidoId);
            if (esEliminatoriaEmpatada(game)) await openPenalties(game); else finish();
        }
        catch (error) { alert(error.message); }
    });

    async function loadQuestion() {
        stopTimer();
        game = await api.estado(session.usuarioId, game.partidoId);
        const assignment = await api.pregunta(session.usuarioId, game.partidoId);
        if (!assignment) {
            game = await api.finalizar(session.usuarioId, game.partidoId);
            if (esEliminatoriaEmpatada(game)) await openPenalties(game); else finish();
            return;
        }
        question = assignment.pregunta;
        options = question.tipoPregunta === "opcion_multiple" ? await api.opciones(session.usuarioId, game.partidoId) : [];
        const used = await api.comodinesUsados(session.usuarioId, game.partidoId);
        const competitor = game.competicion || {};
        ui.local.textContent = game.golesLocal;
        ui.away.textContent = game.golesVisitante;
        ui.localCode.textContent = abbrev(game.equipoLocal?.nombre);
        ui.awayCode.textContent = abbrev(game.equipoVisitante?.nombre);
        ui.competition.textContent = (competitor.nombre || competition).toUpperCase();
        ui.cup.alt = competitor.nombre || "Competición";
        ui.cup.src = `../assets/copa-${competition}.png`;
        ui.period.textContent = `PREGUNTA ${assignment.numeroPregunta}`;
        ui.question.textContent = question.textoPregunta;
        ui.question.classList.toggle("is-penalty-question", question.tipoPregunta === "penal_numerico");
        document.body.classList.toggle("match-penalty-mode", question.tipoPregunta === "penal_numerico");
        ui.answers.replaceChildren();
        if (question.tipoPregunta === "penal_numerico") renderNumeric();
        else options.forEach((option, index) => renderOption(option, index));
        document.querySelectorAll(".wildcard-btn[data-wildcard]").forEach((button) => {
            button.disabled = used.includes(button.dataset.wildcard) || question.tipoPregunta !== "opcion_multiple";
        });
        startedAt = new Date(assignment.preguntaIniciadaEn).getTime();
        seconds = Math.max(0, 15 - Math.floor((Date.now() - startedAt) / 1000));
        startTimer();
    }

    function renderOption(option, index) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "answer-btn";
        const letter = document.createElement("span"); letter.textContent = String.fromCharCode(65 + index);
        const label = document.createElement("strong"); label.textContent = option.textoOpcion;
        button.append(letter, label);
        button.addEventListener("click", () => submit({ preguntaId: question.preguntaId, opcionRespuestaId: option.opcionRespuestaId }));
        ui.answers.appendChild(button);
    }

    function renderNumeric() {
        const form = document.createElement("form");
        form.className = "penalty-answer";
        form.innerHTML = '<label for="penalty-input">Respuesta de aproximación</label><div><input id="penalty-input" type="number" inputmode="decimal" step="any" required><button type="submit">PATEAR</button></div>';
        form.addEventListener("submit", (event) => {
            event.preventDefault();
            submit({ preguntaId: question.preguntaId, textoRespuesta: form.querySelector("input").value });
        });
        ui.answers.appendChild(form);
        form.querySelector("input").focus();
    }

    async function submit(answer) {
        if (busy) return;
        busy = true; stopTimer(); disableAnswers(true);
        try {
            const elapsed = Math.min(15, Math.max(0, Math.ceil((Date.now() - startedAt) / 1000)));
            game = await api.responder(session.usuarioId, game.partidoId, { ...answer, segundos: elapsed });
            ui.local.textContent = game.golesLocal; ui.away.textContent = game.golesVisitante;
            await new Promise((resolve) => window.setTimeout(resolve, 450));
            await loadQuestion();
        } catch (error) {
            alert(error.message);
            disableAnswers(false);
            await loadQuestion().catch((loadError) => alert(loadError.message));
        } finally { busy = false; }
    }

    function startTimer() {
        ui.timer.textContent = `${seconds} SEGUNDOS`;
        interval = window.setInterval(async () => {
            seconds = Math.max(0, 15 - Math.floor((Date.now() - startedAt) / 1000));
            ui.timer.textContent = `${seconds} SEGUNDOS`;
            if (!seconds) {
                stopTimer(); busy = true;
                try { await loadQuestion(); }
                catch (error) { alert(error.message); }
                finally { busy = false; }
            }
        }, 250);
    }
    function stopTimer() { if (interval) window.clearInterval(interval); interval = null; }
    function disableAnswers(disabled) { ui.answers.querySelectorAll("button,input").forEach((item) => { item.disabled = disabled; }); }
    function abbrev(name = "Equipo") { return name.replace(/club|atletico|juniors|plate/gi, "").trim().slice(0, 3).toUpperCase(); }
    function finish() {
        const current = window.FDM.session.get() || {};
        window.FDM.session.set({ ...current, ultimoPartidoId: game.partidoId, partidoActualId: null });
        window.location.assign("resultado-partido.html");
    }
    function esEliminatoriaEmpatada(partido) {
        return ["libertadores", "sudamericana"].includes(partido?.competicion?.codigo)
            && partido.golesLocal === partido.golesVisitante;
    }
    async function openPenalties(partido) {
        try {
            await penalesApi.iniciar(session.usuarioId, partido.partidoId);
            const current = window.FDM.session.get() || {};
            window.FDM.session.set({ ...current, partidoActualId: partido.partidoId, ultimoPartidoId: null });
            window.location.assign("penales.html");
        } catch (error) { alert(error.message); }
    }
});
