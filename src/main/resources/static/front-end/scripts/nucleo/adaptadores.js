(function () {
    const equipo = (value) => value ? {
        equipoId: Number(value.equipoId), codigoExterno: value.codigoExterno || "", nombre: value.nombre || "Equipo",
        apodo: value.apodo || "", pais: value.pais || "", prestigio: Number(value.prestigio) || 1,
        sueldo: Number(value.sueldo) || 0, esInternacional: Boolean(value.esInternacional), rutaEscudo: value.rutaEscudo || null
    } : null;
    const carrera = (value) => value ? {
        carreraId: Number(value.carreraId), nombreDt: value.nombreDt || "", apellidoDt: value.apellidoDt || "",
        edadDt: Number(value.edadDt) || 0, provincia: value.provincia || "", numeroAvatar: Number(value.numeroAvatar) || 1,
        experiencia: Number(value.experiencia) || 0, estrellas: Number(value.estrellas) || 1,
        numeroTemporada: Number(value.numeroTemporada) || 1, objetivoContrato: value.objetivoContrato || "",
        estadoContrato: value.estadoContrato || "activo", equipoActual: equipo(value.equipoActual)
    } : null;
    const competicion = (value) => value ? {
        competicionId: Number(value.competicionId), codigo: value.codigo || "", nombre: value.nombre || value.codigo || "Competición",
        tipoCompeticion: value.tipoCompeticion || "", modificadorDificultad: Number(value.modificadorDificultad) || 0
    } : null;
    const opcion = (value) => value ? {
        opcionRespuestaId: Number(value.opcionRespuestaId), ordenOpcion: Number(value.ordenOpcion) || 0,
        textoOpcion: value.textoOpcion || ""
    } : null;
    const partido = (value) => value ? {
        partidoId: Number(value.partidoId), estado: value.estado || "", iniciadoEn: value.iniciadoEn || null,
        finalizadoEn: value.finalizadoEn || null, duracionSegundos: value.duracionSegundos ?? null,
        variacionExperiencia: Number(value.variacionExperiencia) || 0,
        respuestasCorrectas: Number(value.respuestasCorrectas) || 0, respuestasIncorrectas: Number(value.respuestasIncorrectas) || 0,
        golesLocal: Number(value.golesLocal) || 0,
        golesVisitante: Number(value.golesVisitante) || 0,
        equipoLocal: equipo(value.equipoLocal), equipoVisitante: equipo(value.equipoVisitante), competicion: competicion(value.competicion)
    } : null;
    const fixture = (value) => value ? {
        ...value, fixtureId: Number(value.fixtureId), equipoLocal: equipo(value.equipoLocal),
        equipoVisitante: equipo(value.equipoVisitante), partidoId: value.partidoId == null ? null : Number(value.partidoId),
        equipoGanadorId: value.equipoGanadorId == null ? null : Number(value.equipoGanadorId)
    } : null;
    const temporada = (value) => value ? {
        ...value, fixtures: (value.fixtures || []).map(fixture),
        tabla: (value.tabla || []).map(row => ({ ...row, equipo: equipo(row.equipo) }))
    } : null;
    const torneo = (value) => value ? {
        ...value, torneoId: value.torneoId == null ? null : Number(value.torneoId),
        campeon: equipo(value.campeon), fixtures: (value.fixtures || []).map(fixture)
    } : null;
    const partidoPregunta = (value) => value ? {
        ...value, pregunta: value.pregunta ? {
            preguntaId: Number(value.pregunta.preguntaId), textoPregunta: value.pregunta.textoPregunta || "",
            tipoPregunta: value.pregunta.tipoPregunta || "opcion_multiple"
        } : null
    } : null;
    const tandaPenales = (value) => value ? {
        ...value, partidoId: Number(value.partidoId), numeroTurnoActual: Number(value.numeroTurnoActual),
        golesJugador: Number(value.golesJugador) || 0, golesRival: Number(value.golesRival) || 0,
        equipoGanadorId: value.equipoGanadorId == null ? null : Number(value.equipoGanadorId),
        preguntaActual: value.preguntaActual ? {
            ...value.preguntaActual, preguntaId: Number(value.preguntaActual.preguntaId),
            opciones: (value.preguntaActual.opciones || []).map(opcion)
        } : null,
        turnos: (value.turnos || []).map(turno => ({ ...turno, numeroTurno: Number(turno.numeroTurno) }))
    } : null;

    window.FDM = window.FDM || {};
    window.FDM.mappers = { equipo, carrera, competicion, opcion, partido, fixture, temporada, torneo, partidoPregunta, tandaPenales };
})();
