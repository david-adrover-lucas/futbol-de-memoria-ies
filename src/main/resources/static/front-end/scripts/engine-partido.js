(function () {
    const tiempos = ["10'", "20'", "30'", "40'", "45'", "55'", "65'", "75'", "85'", "90'"];
    const competiciones = {
        temporada: {
            id: "temporada",
            nombre: "Temporada",
            copa: "../assets/copa-temporada.png",
            dificultad: 0,
            objetivo: "Sumar puntos en liga"
        },
        nacional: {
            id: "nacional",
            nombre: "Copa Nacional",
            copa: "../assets/copa-nacional.png",
            dificultad: 1,
            objetivo: "Avanzar de ronda"
        },
        sudamericana: {
            id: "sudamericana",
            nombre: "Copa Sudamericana",
            copa: "../assets/copa-sudamericana.png",
            dificultad: 2,
            objetivo: "Competir internacionalmente"
        },
        libertadores: {
            id: "libertadores",
            nombre: "Copa Libertadores",
            copa: "../assets/copa-libertadores.png",
            dificultad: 3,
            objetivo: "Ganar la gloria continental"
        }
    };

    const preguntasPenal = [
        { texto: "PENAL DE APROXIMACION: CUANTAS COPAS LIBERTADORES TIENE INDEPENDIENTE?", valor: 7, tolerancia: 0 },
        { texto: "PENAL DE APROXIMACION: CUANTOS PUNTOS VALE UNA VICTORIA EN LIGA?", valor: 3, tolerancia: 0 },
        { texto: "PENAL DE APROXIMACION: CUANTOS JUGADORES TITULARES TIENE UN EQUIPO?", valor: 11, tolerancia: 0 },
        { texto: "PENAL DE APROXIMACION: CUANTOS MINUTOS DURA UN PARTIDO SIN ADICION?", valor: 90, tolerancia: 0 }
    ];

    function mezclar(lista) {
        return [...lista].sort(() => Math.random() - 0.5);
    }

    function crearPartido() {
        const storage = window.FDM.storage;
        const club = storage.getClub();
        const competicion = obtenerCompeticion(storage);
        const rival = obtenerRival(storage, club, competicion);
        const dificultadRival = Math.min(5, (Number(rival.stars) || 1) + competicion.dificultad);
        const pool = window.FDM.preguntas.paraRival(dificultadRival);
        const preguntas = mezclar(pool).slice(0, 10);

        return {
            id: `partido-${Date.now()}`,
            competicion,
            club,
            rival,
            preguntaActual: 0,
            tiempo: tiempos[0],
            golesLocal: 0,
            golesRival: 0,
            aciertos: 0,
            errores: 0,
            comodines: {
                doble: 1,
                penal: 1,
                cincuenta: 1
            },
            comodinesRival: {
                doble: Math.max(1, Math.min(3, Math.ceil((Number(rival.stars) || 1) / 2))),
                penal: Math.max(1, Math.min(2, Math.ceil(((Number(rival.stars) || 1) + competicion.dificultad) / 4)))
            },
            dobleActivo: false,
            dobleRivalActivo: false,
            preguntas,
            eventos: [],
            ultimoEvento: null,
            estado: "en-curso"
        };
    }

    function obtenerCompeticion(storage) {
        const torneoActivo = storage.read(storage.KEYS.torneos, null)?.activo;
        return competiciones[torneoActivo] || competiciones.temporada;
    }

    function obtenerRival(storage, club, competicion) {
        if (competicion.id === "temporada") return window.FDM.clubes.randomOpponent(club.id);

        const torneo = storage.read(storage.KEYS.torneos, null);
        const participantes = Array.isArray(torneo?.participantes) ? torneo.participantes : [];
        const candidatos = participantes.filter((item) => item && item.id !== club.id);

        if (candidatos.length) return candidatos[Math.floor(Math.random() * candidatos.length)];
        return window.FDM.clubes.randomOpponent(club.id);
    }

    function probabilidadGol(acierto, partido) {
        const rivalStars = Number(partido.rival.stars) || 1;
        const dificultad = Number(partido.competicion?.dificultad) || 0;
        if (acierto) return Math.max(0.22, 0.66 - rivalStars * 0.052 - dificultad * 0.025);
        return Math.min(0.66, 0.2 + rivalStars * 0.056 + dificultad * 0.026);
    }

    function probabilidadComodinRival(partido, tipo) {
        const stars = Number(partido.rival.stars) || 1;
        const dificultad = Number(partido.competicion?.dificultad) || 0;
        const base = tipo === "doble" ? 0.04 : 0.035;
        const porPrestigio = stars * (tipo === "doble" ? 0.045 : 0.04);
        const porCompeticion = dificultad * 0.025;
        return Math.min(tipo === "doble" ? 0.34 : 0.28, base + porPrestigio + porCompeticion);
    }

    function crearPreguntaPenal(ejecutor) {
        const base = preguntasPenal[Math.floor(Math.random() * preguntasPenal.length)];
        return {
            texto: base.texto,
            opciones: [],
            correcta: null,
            valor: base.valor,
            tolerancia: Number(base.tolerancia) || 0,
            tipo: "penal",
            subtipo: "aproximacion",
            ejecutor
        };
    }

    function insertarPenal(partido, ejecutor, inmediato) {
        const indice = inmediato
            ? partido.preguntaActual
            : Math.min(partido.preguntaActual + 1, partido.preguntas.length);
        partido.preguntas.splice(indice, 0, crearPreguntaPenal(ejecutor));
        registrarEvento(
            partido,
            ejecutor === "jugador" ? "penal-favor" : "penal-rival",
            ejecutor === "jugador" ? "PENAL A FAVOR" : "PENAL RIVAL",
            ejecutor === "jugador" ? partido.club.name.toUpperCase() : partido.rival.name.toUpperCase()
        );
        return partido;
    }

    function responder(partido, indiceRespuesta) {
        const pregunta = partido.preguntas[partido.preguntaActual];
        const acierto = pregunta.tipo === "penal"
            ? respuestaPenalCorrecta(pregunta, indiceRespuesta)
            : Number(indiceRespuesta) === pregunta.correcta;
        const minuto = tiempos[partido.preguntaActual] || "90'";

        if (pregunta.tipo === "penal") {
            if (pregunta.ejecutor === "jugador") {
                if (acierto) {
                    partido.golesLocal += partido.dobleActivo ? 2 : 1;
                    registrarEvento(partido, "penal-convertido", "PENAL CONVERTIDO", "Gol desde los doce pasos");
                } else {
                    registrarEvento(partido, "penal-fallado", "PENAL FALLADO", "El arquero gana el duelo");
                }
            } else if (acierto) {
                registrarEvento(partido, "penal-atajado", "PENAL ATAJADO", "Respuesta correcta, tapada enorme");
            } else {
                partido.golesRival += 1;
                registrarEvento(partido, "penal-rival-gol", "GOL DE PENAL", partido.rival.name.toUpperCase());
            }

            partido.dobleActivo = false;
            partido.dobleRivalActivo = false;
            avanzar(partido);
            return partido;
        }

        if (acierto) {
            partido.aciertos += 1;
            partido.golesLocal += partido.dobleActivo ? 2 : 1;
            registrarEvento(
                partido,
                partido.dobleActivo ? "gol-doble" : "gol",
                partido.dobleActivo ? "GOL DOBLE" : "GOL",
                "Respuesta correcta"
            );
        } else {
            partido.errores += 1;
            resolverAtaqueRivalPorError(partido, minuto);
        }

        partido.dobleActivo = false;
        avanzar(partido);
        return partido;
    }

    function respuestaPenalCorrecta(pregunta, respuesta) {
        const valor = Number(String(respuesta).replace(",", "."));
        if (!Number.isFinite(valor)) return false;
        return Math.abs(valor - Number(pregunta.valor)) <= (Number(pregunta.tolerancia) || 0);
    }

    function resolverAtaqueRivalPorError(partido, minuto) {
        const chancePenal = partido.comodinesRival.penal > 0
            ? probabilidadComodinRival(partido, "penal")
            : 0;

        if (chancePenal > 0 && Math.random() < chancePenal) {
            partido.comodinesRival.penal -= 1;
            insertarPenal(partido, "rival");
            return;
        }

        const chanceDoble = partido.comodinesRival.doble > 0
            ? probabilidadComodinRival(partido, "doble")
            : 0;

        if (chanceDoble > 0 && Math.random() < chanceDoble) {
            partido.comodinesRival.doble -= 1;
            partido.golesRival += 2;
            registrarEvento(partido, "gol-doble-rival", "GOL DOBLE RIVAL", "Respuesta incorrecta");
            return;
        }

        partido.golesRival += 1;
        registrarEvento(partido, "gol-rival", "GOL RIVAL", "Respuesta incorrecta");
    }

    function avanzar(partido) {
        partido.preguntaActual += 1;
        partido.tiempo = tiempos[Math.min(partido.preguntaActual, tiempos.length - 1)] || "FT";

        if (partido.preguntaActual >= partido.preguntas.length) {
            partido.estado = "finalizado";
            partido.tiempo = "FT";
        }
    }

    function usarComodin(partido, tipo) {
        if (!partido.comodines[tipo]) return partido;
        partido.comodines[tipo] -= 1;

        if (tipo === "doble") {
            partido.dobleActivo = true;
            registrarEvento(partido, "comodin-doble", "GOL DOBLE ACTIVADO", "Tu proximo gol vale por dos");
        }

        if (tipo === "penal") {
            insertarPenal(partido, "jugador", true);
        }

        return partido;
    }

    function resultado(partido) {
        if (partido.golesLocal > partido.golesRival) return "victoria";
        if (partido.golesLocal < partido.golesRival) return "derrota";
        return "empate";
    }

    function registrarEvento(partido, tipo, titulo, detalle) {
        const evento = {
            minuto: partido.tiempo,
            tipo,
            titulo,
            detalle
        };

        partido.ultimoEvento = evento;
        partido.eventos.push(`${evento.minuto} ${evento.titulo}${evento.detalle ? " - " + evento.detalle : ""}`);
        return evento;
    }

    window.FDM = window.FDM || {};
    window.FDM.enginePartido = { crearPartido, responder, usarComodin, resultado };
})();
