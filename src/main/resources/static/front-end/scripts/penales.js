document.addEventListener("DOMContentLoaded", async () => {
    const session = window.FDM.session.requireUser();
    if (!session || !session.partidoActualId) { window.location.replace("menu.html"); return; }
    const { tandaPenalesService, partidoService } = window.FDM.services;
    const ui = {
        player: document.getElementById("shootout-player-score"), rival: document.getElementById("shootout-rival-score"),
        local: document.getElementById("shootout-local-code"), away: document.getElementById("shootout-away-code"),
        phase: document.getElementById("shootout-phase"), round: document.getElementById("shootout-round"),
        role: document.getElementById("shootout-role"), timer: document.getElementById("shootout-timer"),
        question: document.getElementById("shootout-question"), answers: document.getElementById("shootout-answers")
    };
    let tanda, partido, startedAt = 0, interval = null, busy = false;

    try {
        [partido, tanda] = await Promise.all([
            partidoService.estado(session.usuarioId, session.partidoActualId),
            tandaPenalesService.iniciar(session.usuarioId, session.partidoActualId)
        ]);
        ui.local.textContent = abbrev(partido.equipoLocal?.nombre);
        ui.away.textContent = abbrev(partido.equipoVisitante?.nombre);
        await render();
    } catch (error) { alert(error.message); window.location.assign("menu.html"); return; }

    async function render() {
        stopTimer();
        if (tanda.estado === "finalizada") { finish(); return; }
        const current = tanda.preguntaActual;
        ui.player.textContent = tanda.golesJugador;
        ui.rival.textContent = tanda.golesRival;
        ui.phase.textContent = tanda.faseActual === "muerte_subita" ? "MUERTE SÚBITA" : "TANDA DE PENALES";
        ui.round.textContent = tanda.faseActual === "regular"
            ? `TURNO ${tanda.numeroTurnoActual} DE 10`
            : `MUERTE SÚBITA · TURNO ${tanda.numeroTurnoActual - 10}`;
        const turn = tanda.turnos.find(item => item.numeroTurno === tanda.numeroTurnoActual);
        ui.role.textContent = turn?.rolJugador === "ataja" ? "ATAJÁS EL PENAL" : "PATEÁS EL PENAL";
        if (!current) {
            ui.role.textContent = "TANDA GUARDADA";
            ui.question.textContent = "Se agotaron las preguntas sin repetir. Agregá más preguntas activas y recargá esta pantalla para continuar.";
            ui.answers.replaceChildren();
            return;
        }
        ui.question.textContent = current.textoPregunta;
        ui.question.classList.toggle("is-penalty-question", true);
        ui.answers.replaceChildren();
        if (current.tipoPregunta === "opcion_multiple") current.opciones.forEach((option, index) => renderOption(current, option, index));
        else renderNumeric(current);
        startedAt = new Date(current.preguntaIniciadaEn).getTime();
        startTimer();
    }

    function renderOption(question, option, index) {
        const button = document.createElement("button"); button.type = "button"; button.className = "answer-btn";
        const letter = document.createElement("span"); letter.textContent = String.fromCharCode(65 + index);
        const label = document.createElement("strong"); label.textContent = option.textoOpcion;
        button.append(letter, label);
        button.addEventListener("click", () => submit({ preguntaId: question.preguntaId, opcionRespuestaId: option.opcionRespuestaId }));
        ui.answers.appendChild(button);
    }

    function renderNumeric(question) {
        const form = document.createElement("form"); form.className = "penalty-answer";
        form.innerHTML = '<label for="shootout-input">Respuesta numérica</label><div><input id="shootout-input" type="number" inputmode="decimal" step="any" required><button type="submit">CONFIRMAR</button></div>';
        form.addEventListener("submit", event => {
            event.preventDefault(); submit({ preguntaId: question.preguntaId, textoRespuesta: form.querySelector("input").value });
        });
        ui.answers.appendChild(form); form.querySelector("input").focus();
    }

    async function submit(answer) {
        if (busy) return;
        busy = true; stopTimer(); disable(true);
        try {
            const elapsed = Math.min(15, Math.max(0, Math.ceil((Date.now() - startedAt) / 1000)));
            tanda = await tandaPenalesService.responder(session.usuarioId, partido.partidoId, { ...answer, segundos: elapsed });
            await new Promise(resolve => window.setTimeout(resolve, 350));
            await render();
        } catch (error) {
            alert(error.message);
            try { tanda = await tandaPenalesService.obtener(session.usuarioId, partido.partidoId); await render(); }
            catch (loadError) { alert(loadError.message); }
        } finally { busy = false; }
    }

    function startTimer() {
        interval = window.setInterval(async () => {
            const seconds = Math.max(0, 15 - Math.floor((Date.now() - startedAt) / 1000));
            ui.timer.textContent = `${seconds} SEGUNDOS`;
            if (seconds === 0 && !busy) {
                busy = true; stopTimer();
                try { tanda = await tandaPenalesService.obtener(session.usuarioId, partido.partidoId); await render(); }
                catch (error) { alert(error.message); }
                finally { busy = false; }
            }
        }, 250);
    }
    function stopTimer() { if (interval) window.clearInterval(interval); interval = null; }
    function disable(value) { ui.answers.querySelectorAll("button,input").forEach(element => element.disabled = value); }
    function abbrev(name = "Equipo") { return name.replace(/club|atletico|juniors|plate/gi, "").trim().slice(0, 3).toUpperCase(); }
    function finish() {
        const current = window.FDM.session.get() || {};
        window.FDM.session.set({ ...current, ultimoPartidoId: partido.partidoId, partidoActualId: null });
        window.location.assign("resultado-partido.html");
    }
});
