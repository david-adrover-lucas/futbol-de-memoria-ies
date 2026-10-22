(function () {
    const api = window.FDM.api;
    const map = window.FDM.mappers;
    const id = (value) => encodeURIComponent(value);

    const authService = {
        login: (nombreUsuario, contrasena) => api.request("/auth/login", { method: "POST", body: { nombreUsuario, contrasena } }),
        register: (nombreUsuario, correoElectronico, contrasena) => api.request("/auth/registro", {
            method: "POST", body: { nombreUsuario, correoElectronico, contrasena }
        })
    };
    const usuarioService = {
        obtener: (usuarioId) => api.request(`/usuarios/${id(usuarioId)}`),
        actualizar: (usuarioId, perfil) => api.request(`/usuarios/${id(usuarioId)}`, { method: "PUT", body: perfil })
    };
    const carreraService = {
        obtener: (usuarioId) => api.request(`/usuarios/${id(usuarioId)}/carrera`).then(map.carrera),
        crear: (usuarioId, perfil) => api.request(`/usuarios/${id(usuarioId)}/carrera`, { method: "POST", body: perfil }).then(map.carrera),
        cambiarEquipo: (carreraId, equipoId) => api.request(`/carreras/${id(carreraId)}/equipo`, { method: "PUT", body: { equipoId } }).then(map.carrera)
    };
    const equipoService = {
        seleccionables: (estrellas) => api.request(`/equipos/seleccionables?estrellas=${id(estrellas)}`).then(items => items.map(map.equipo)),
        listar: (params = {}) => api.request(`/equipos?${new URLSearchParams(params)}`).then(page => ({ ...page, content: (page.content || []).map(map.equipo) }))
    };
    const competicionService = {
        listar: (tipo) => api.request(`/competiciones${tipo ? `?tipo=${id(tipo)}` : ""}`).then(items => items.map(map.competicion)),
        obtener: (codigo) => api.request(`/competiciones/${id(codigo)}`).then(map.competicion)
    };
    const partidoService = {
        iniciar: (usuarioId, codigoCompeticion, fixture = {}) => api.request(`/usuarios/${id(usuarioId)}/partidos`, { method: "POST", body: { codigoCompeticion, ...fixture } }).then(map.partido),
        estado: (usuarioId, partidoId) => api.request(`/usuarios/${id(usuarioId)}/partidos/${id(partidoId)}`).then(map.partido),
        pregunta: (usuarioId, partidoId) => api.request(`/usuarios/${id(usuarioId)}/partidos/${id(partidoId)}/pregunta-actual`).then(map.partidoPregunta),
        opciones: (usuarioId, partidoId) => api.request(`/usuarios/${id(usuarioId)}/partidos/${id(partidoId)}/opciones`).then(items => items.map(map.opcion)),
        responder: (usuarioId, partidoId, respuesta) => api.request(`/usuarios/${id(usuarioId)}/partidos/${id(partidoId)}/respuestas`, { method: "POST", body: respuesta }).then(map.partido),
        comodin: (usuarioId, partidoId, tipoComodin) => api.request(`/usuarios/${id(usuarioId)}/partidos/${id(partidoId)}/comodines`, { method: "POST", body: { tipoComodin } }),
        comodinesUsados: (usuarioId, partidoId) => api.request(`/usuarios/${id(usuarioId)}/partidos/${id(partidoId)}/comodines`),
        finalizar: (usuarioId, partidoId) => api.request(`/usuarios/${id(usuarioId)}/partidos/${id(partidoId)}/finalizar`, { method: "POST" }).then(map.partido),
        historial: (usuarioId, partidoId) => api.request(`/usuarios/${id(usuarioId)}/partidos/${id(partidoId)}/intentos`)
    };
    const tandaPenalesService = {
        iniciar: (usuarioId, partidoId) => api.request(`/usuarios/${id(usuarioId)}/partidos/${id(partidoId)}/penales`, { method: "POST" }).then(map.tandaPenales),
        obtener: (usuarioId, partidoId) => api.request(`/usuarios/${id(usuarioId)}/partidos/${id(partidoId)}/penales`).then(map.tandaPenales),
        responder: (usuarioId, partidoId, respuesta) => api.request(`/usuarios/${id(usuarioId)}/partidos/${id(partidoId)}/penales/respuestas`, { method: "POST", body: respuesta }).then(map.tandaPenales)
    };
    const estadisticaService = {
        obtener: (usuarioId) => api.request(`/usuarios/${id(usuarioId)}/estadisticas`),
        historial: (usuarioId, competicionId) => api.request(`/usuarios/${id(usuarioId)}/historial${competicionId ? `?competicionId=${id(competicionId)}` : ""}`).then(items => items.map(map.partido)),
        ranking: (pagina = 0, tamano = 20) => api.request(`/ranking?pagina=${pagina}&tamano=${tamano}`).then(page => ({ ...page, content: (page.content || []).map(map.carrera) }))
    };
    const temporadaService = {
        obtener: (usuarioId) => api.request(`/usuarios/${id(usuarioId)}/temporada`).then(map.temporada),
        siguientePartido: (usuarioId) => api.request(`/usuarios/${id(usuarioId)}/temporada/partido-siguiente`, { method: "POST" }).then(map.fixture)
    };
    const torneoService = {
        obtener: (usuarioId, codigo) => api.request(`/usuarios/${id(usuarioId)}/torneos/${id(codigo)}`).then(map.torneo),
        iniciar: (usuarioId, codigo) => api.request(`/usuarios/${id(usuarioId)}/torneos/${id(codigo)}`, { method: "POST" }).then(map.torneo),
        partido: (usuarioId, codigo) => api.request(`/usuarios/${id(usuarioId)}/torneos/${id(codigo)}/partido`, { method: "POST" }).then(map.fixture)
    };
    const catalogoService = {
        niveles: () => api.request("/niveles-dificultad"),
        competiciones: () => competicionService.listar()
    };

    window.FDM = window.FDM || {};
    window.FDM.services = { authService, usuarioService, carreraService, equipoService, competicionService, partidoService, tandaPenalesService, estadisticaService, temporadaService, torneoService, catalogoService };
})();
