document.addEventListener("DOMContentLoaded", () => {
    const storage = window.FDM.storage;
    const engine = window.FDM.enginePartido;
    let partido = engine.crearPartido();
    let timer = 15;
    let intervalId = null;

    const scoreLocal = document.getElementById("score-local-goals");
    const scoreAway = document.getElementById("score-away-goals");
    const scoreLocalCode = document.getElementById("score-local-code");
    const scoreAwayCode = document.getElementById("score-away-code");
    const scorePeriod = document.getElementById("score-period");
    const scoreCup = document.getElementById("score-cup");
    const scoreCompetition = document.getElementById("score-competition");
    const questionText = document.getElementById("question-text");
    const answersGrid = document.getElementById("answers-grid");
    const questionTimer = document.getElementById("question-timer");
    const eventLayer = document.getElementById("match-event-layer");

    scoreLocalCode.textContent = codigo(partido.club.name);
    scoreAwayCode.textContent = codigo(partido.rival.name);
    scoreCup.src = partido.competicion?.copa || copaActiva();
    scoreCup.alt = partido.competicion?.nombre || "Competicion";
    scoreCompetition.textContent = (partido.competicion?.nombre || "Temporada").toUpperCase();

    document.querySelectorAll(".wildcard-btn[data-wildcard]").forEach((button) => {
        button.addEventListener("click", () => {
            const tipo = button.dataset.wildcard;
            partido = engine.usarComodin(partido, tipo);
            button.disabled = !partido.comodines[tipo];

            mostrarEvento(partido.ultimoEvento);
            if (tipo === "cincuenta") ocultarDosIncorrectas();
            if (tipo === "penal") render();
        });
    });

    document.getElementById("btn-exit-match").addEventListener("click", () => {
        window.location.href = "menu.html";
    });

    render();

    function render() {
        if (partido.estado === "finalizado") {
            storage.write(storage.KEYS.ultimoPartido, partido);
            window.location.href = "resultado-partido.html";
            return;
        }

        const pregunta = partido.preguntas[partido.preguntaActual];
        timer = 15;
        scoreLocal.textContent = partido.golesLocal;
        scoreAway.textContent = partido.golesRival;
        scorePeriod.innerHTML = `${partido.preguntaActual < 5 ? "PT" : "ST"}&nbsp;&nbsp;${partido.tiempo}`;
        questionText.textContent = pregunta.texto;
        questionText.classList.toggle("is-penalty-question", pregunta.tipo === "penal");
        document.body.classList.toggle("match-penalty-mode", pregunta.tipo === "penal");
        answersGrid.innerHTML = "";

        if (pregunta.tipo === "penal") {
            renderPenalInput();
        } else {
            pregunta.opciones.forEach((opcion, index) => {
                const button = document.createElement("button");
                button.type = "button";
                button.className = "answer-btn";
                button.innerHTML = `<span>${String.fromCharCode(65 + index)}</span><strong>${opcion}</strong>`;
                button.addEventListener("click", () => elegirRespuesta(index));
                answersGrid.appendChild(button);
            });
        }

        document.querySelectorAll(".wildcard-btn[data-wildcard]").forEach((button) => {
            button.disabled = pregunta.tipo === "penal" || !partido.comodines[button.dataset.wildcard];
        });

        arrancarTimer();
    }

    function renderPenalInput() {
        const form = document.createElement("form");
        form.className = "penalty-answer";
        form.innerHTML = `
            <label for="penalty-input">Respuesta de aproximacion</label>
            <div>
                <input id="penalty-input" type="number" inputmode="numeric" autocomplete="off" placeholder="0">
                <button type="submit">PATEAR</button>
            </div>
        `;
        form.addEventListener("submit", (event) => {
            event.preventDefault();
            const input = form.querySelector("input");
            elegirRespuesta(input.value);
        });
        answersGrid.appendChild(form);
        form.querySelector("input").focus();
    }

    function elegirRespuesta(index) {
        detenerTimer();
        answersGrid.querySelectorAll("button, input").forEach((control) => {
            control.disabled = true;
        });
        partido = engine.responder(partido, index);
        scoreLocal.textContent = partido.golesLocal;
        scoreAway.textContent = partido.golesRival;
        mostrarEvento(partido.ultimoEvento);
        setTimeout(render, 650);
    }

    function arrancarTimer() {
        detenerTimer();
        questionTimer.textContent = `${timer} SEGUNDOS`;
        intervalId = setInterval(() => {
            timer -= 1;
            questionTimer.textContent = `${timer} SEGUNDOS`;

            if (timer <= 0) {
                elegirRespuesta(-1);
            }
        }, 1000);
    }

    function detenerTimer() {
        if (intervalId) clearInterval(intervalId);
        intervalId = null;
    }

    function ocultarDosIncorrectas() {
        const pregunta = partido.preguntas[partido.preguntaActual];
        if (pregunta.tipo === "penal") return;
        const botones = [...answersGrid.querySelectorAll(".answer-btn")];
        botones
            .filter((_, index) => index !== pregunta.correcta)
            .slice(0, 2)
            .forEach((button) => {
                button.disabled = true;
                button.classList.add("answer-hidden");
            });
        mostrarEvento({
            tipo: "comodin-5050",
            titulo: "50/50 ACTIVADO",
            detalle: "Dos respuestas incorrectas eliminadas"
        });
    }

    function mostrarEvento(evento) {
        if (!evento) return;

        eventLayer.innerHTML = `
            <div class="match-event-card match-event-${evento.tipo}">
                <span>${evento.minuto || ""}</span>
                <strong>${evento.titulo}</strong>
                <em>${evento.detalle || ""}</em>
            </div>
        `;

        eventLayer.classList.remove("is-visible");
        document.body.classList.remove("match-score-flash", "match-danger-flash", "match-penalty-flash");
        void eventLayer.offsetWidth;
        eventLayer.classList.add("is-visible");

        const dangerTypes = ["gol-rival", "gol-doble-rival", "penal-rival-gol"];
        const penaltyTypes = ["penal-favor", "penal-rival", "penal-atajado", "penal-convertido", "penal-fallado"];
        const bodyClass = dangerTypes.includes(evento.tipo)
            ? "match-danger-flash"
            : penaltyTypes.includes(evento.tipo)
                ? "match-penalty-flash"
                : "match-score-flash";
        document.body.classList.add(bodyClass);

        window.setTimeout(() => {
            eventLayer.classList.remove("is-visible");
            document.body.classList.remove(bodyClass);
        }, 1150);
    }

    function codigo(nombre) {
        return nombre
            .replace(/Club Atletico|Juniors|Plate|Club/gi, "")
            .trim()
            .slice(0, 3)
            .toUpperCase();
    }

    function copaActiva() {
        const torneo = storage.read(storage.KEYS.torneos, null)?.activo;
        const copas = {
            libertadores: "../assets/copa-libertadores.png",
            sudamericana: "../assets/copa-sudamericana.png",
            nacional: "../assets/copa-nacional.png"
        };

        return copas[torneo] || "../assets/copa-libertadores.png";
    }
});
