package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.models.*;
import com.auditoriria_ies.futbol_memoria.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional(readOnly = true)
public class PartidoServices {
    private static final int MAX_PREGUNTAS = 10;
    private static final int TIEMPO_MAXIMO_SEGUNDOS = 15;
    private final PartidoRepository partidoRepository;
    private final PartidoPreguntaRepository partidoPreguntaRepository;
    private final PartidoComodinRepository partidoComodinRepository;
    private final CarreraServices carreraServices;
    private final CompeticionServices competicionServices;
    private final EquipoServices equipoServices;
    private final NivelDificultadServices nivelServices;
    private final PreguntaServices preguntaServices;
    private final OpcionRespuestaServices opcionServices;
    private final IntentoPartidoServices intentoServices;
    private final TemporadaServices temporadaServices;
    private final CarreraTorneoServices torneoServices;

    public PartidoServices(PartidoRepository partidoRepository, PartidoPreguntaRepository partidoPreguntaRepository,
                           PartidoComodinRepository partidoComodinRepository,
                           CarreraServices carreraServices, CompeticionServices competicionServices,
                           EquipoServices equipoServices, NivelDificultadServices nivelServices,
                           PreguntaServices preguntaServices, OpcionRespuestaServices opcionServices,
                           IntentoPartidoServices intentoServices, TemporadaServices temporadaServices,
                           CarreraTorneoServices torneoServices) {
        this.partidoRepository = partidoRepository;
        this.partidoPreguntaRepository = partidoPreguntaRepository;
        this.partidoComodinRepository = partidoComodinRepository;
        this.carreraServices = carreraServices;
        this.competicionServices = competicionServices;
        this.equipoServices = equipoServices;
        this.nivelServices = nivelServices;
        this.preguntaServices = preguntaServices;
        this.opcionServices = opcionServices;
        this.intentoServices = intentoServices;
        this.temporadaServices = temporadaServices;
        this.torneoServices = torneoServices;
    }

    @Transactional
    public Partido iniciarPartido(Long usuarioId, String codigoCompeticion) {
        return iniciarPartido(usuarioId, codigoCompeticion, null, null);
    }

    @Transactional
    public Partido iniciarPartido(Long usuarioId, String codigoCompeticion, Long temporadaFixtureId, Long torneoFixtureId) {
        if (temporadaFixtureId != null && torneoFixtureId != null) throw new ReglaNegocioException("Un partido solo puede pertenecer a una competición");
        Carrera carrera = carreraServices.obtenerCarreraActivaPorUsuario(usuarioId);
        if (carrera.getEquipoActual() == null) throw new ReglaNegocioException("La carrera todavía no tiene un equipo asignado");
        Competicion competicion = competicionServices.obtenerEntidadPorCodigo(codigoCompeticion);
        competicionServices.validarQueLaCompeticionEsteHabilitada(competicion, carrera);
        Equipo rival;
        TemporadaFixture temporadaFixture = null;
        TorneoFixture torneoFixture = null;
        if (temporadaFixtureId != null) {
            if (!"temporada".equals(codigoCompeticion)) throw new ReglaNegocioException("El fixture indicado no corresponde a la temporada");
            temporadaFixture = temporadaServices.validarYObtenerFixture(temporadaFixtureId, usuarioId);
            rival = temporadaFixture.getEquipoVisitante();
        } else if (torneoFixtureId != null) {
            torneoFixture = torneoServices.validarYObtenerFixture(torneoFixtureId, usuarioId, codigoCompeticion);
            rival = torneoFixture.getEquipoVisitante();
        } else {
            rival = equipoServices.buscarRivalAleatorioExcluyendoEquipo(carrera.getEquipoActual().getEquipoId(), null, null, null);
        }
        equipoServices.validarQueLosEquiposSeanDistintos(carrera.getEquipoActual().getEquipoId(), rival.getEquipoId());

        int nivelMaximo = nivelServices.calcularNivelMaximoDePregunta(rival.getPrestigio(), competicionServices.obtenerModificadorDeDificultad(competicion));
        List<Pregunta> elegibles = preguntaServices.listarPreguntasElegibles(nivelMaximo, Set.of());
        int cantidad = Math.min(MAX_PREGUNTAS, elegibles.size());
        if (cantidad == 0) throw new RecursoNoEncontradoException("No hay preguntas disponibles para iniciar el partido");
        List<Pregunta> preguntas = preguntaServices.seleccionarPreguntasParaPartido(cantidad, nivelMaximo, Set.of());

        Partido partido = new Partido();
        partido.setCarrera(carrera);
        partido.setCompeticion(competicion);
        partido.setEquipoLocal(carrera.getEquipoActual());
        partido.setEquipoVisitante(rival);
        partido.setEstado("en_curso");
        partido.setGolesLocal(0);
        partido.setGolesVisitante(0);
        partido.setRespuestasCorrectas(0);
        partido.setRespuestasIncorrectas(0);
        partido.setVariacionExperiencia(0);
        partido.setIniciadoEn(LocalDateTime.now());
        partido = partidoRepository.save(partido);
        if (temporadaFixture != null) temporadaServices.asignarFixture(temporadaFixture, partido);
        if (torneoFixture != null) torneoServices.asignarFixture(torneoFixture, partido);

        List<PartidoPregunta> asignaciones = new ArrayList<>();
        for (int i = 0; i < preguntas.size(); i++) {
            PartidoPregunta asignacion = new PartidoPregunta();
            asignacion.setPartido(partido);
            asignacion.setPregunta(preguntas.get(i));
            asignacion.setNumeroPregunta(i + 1);
            asignacion.setEstado("pendiente");
            asignaciones.add(asignacion);
        }
        partidoPreguntaRepository.saveAll(asignaciones);
        return partido;
    }

    @Transactional
    public Optional<PartidoPregunta> obtenerPreguntaActualDelPartido(Long usuarioId, Long partidoId) {
        Partido partido = buscarPartidoParaActualizar(partidoId);
        validarPropiedadDelPartido(usuarioId, partido);
        validarQueElPartidoEsteEnCurso(partido);
        return obtenerAsignacionActual(partido);
    }

    @Transactional
    public List<OpcionRespuesta> obtenerOpcionesDeLaPreguntaActual(Long usuarioId, Long partidoId) {
        Partido partido = buscarPartidoParaActualizar(partidoId);
        validarPropiedadDelPartido(usuarioId, partido);
        validarQueElPartidoEsteEnCurso(partido);
        Optional<PartidoPregunta> actualOptional = obtenerAsignacionActual(partido);
        if (actualOptional.isEmpty()) return List.of();
        PartidoPregunta actual = actualOptional.get();
        if (!"opcion_multiple".equals(actual.getPregunta().getTipoPregunta())) {
            throw new ReglaNegocioException("La pregunta actual no utiliza opciones múltiples");
        }
        List<OpcionRespuesta> opciones = opcionServices.listarOpcionesDePregunta(actual.getPregunta().getPreguntaId());
        var comodinCincuenta = partidoComodinRepository.findByPartidoPartidoIdAndPreguntaPreguntaIdAndTipoComodin(
                partidoId, actual.getPregunta().getPreguntaId(), "cincuenta");
        if (comodinCincuenta.isEmpty()) return opciones;
        OpcionRespuesta correcta = opciones.stream().filter(o -> Boolean.TRUE.equals(o.getEsCorrecta())).findFirst()
                .orElseThrow(() -> new ReglaNegocioException("La pregunta no tiene una opción correcta configurada"));
        OpcionRespuesta distractor = comodinCincuenta.get().getOpcionDistractor();
        if (distractor == null) throw new ReglaNegocioException("El comodín no tiene un distractor persistido");
        return List.of(correcta, distractor).stream().sorted(Comparator.comparing(OpcionRespuesta::getOrdenOpcion)).toList();
    }

    public List<String> listarTiposDeComodinUsados(Long usuarioId, Long partidoId) {
        Partido partido = buscarPartido(partidoId);
        validarPropiedadDelPartido(usuarioId, partido);
        return partidoComodinRepository.findByPartidoPartidoId(partidoId).stream()
                .map(PartidoComodin::getTipoComodin).toList();
    }

    @Transactional
    public Partido registrarRespuesta(Long usuarioId, Long partidoId, Long preguntaId, Long opcionRespuestaId,
                                      String textoRespuesta, Integer segundosInformadosPorCliente) {
        Partido partido = buscarPartidoParaActualizar(partidoId);
        validarPropiedadDelPartido(usuarioId, partido);
        validarQueElPartidoEsteEnCurso(partido);
        PartidoPregunta actual = primeraPreguntaPendiente(partido);
        if (actual.getPreguntaIniciadaEn() == null) throw new ReglaNegocioException("Primero solicitá la pregunta actual al servidor");
        LocalDateTime ahora = LocalDateTime.now();
        long transcurridoMs = Duration.between(actual.getPreguntaIniciadaEn(), ahora).toMillis();
        if (transcurridoMs > TIEMPO_MAXIMO_SEGUNDOS * 1000L) {
            registrarVencimiento(partido, actual, ahora);
            return partido;
        }
        if (!actual.getPregunta().getPreguntaId().equals(preguntaId)) {
            throw new ReglaNegocioException("La respuesta no corresponde a la pregunta actual del partido");
        }
        int tiempoServidor = validarTiempoDeRespuesta(segundosInformadosPorCliente, actual.getPreguntaIniciadaEn(), ahora);

        boolean correcta;
        if ("opcion_multiple".equals(actual.getPregunta().getTipoPregunta())) {
            if (opcionRespuestaId == null || textoRespuesta != null) throw new ReglaNegocioException("Para esta pregunta enviá una opción");
            correcta = preguntaServices.validarRespuestaSeleccionada(preguntaId, opcionRespuestaId);
        } else if ("penal_numerico".equals(actual.getPregunta().getTipoPregunta())) {
            if (opcionRespuestaId != null || textoRespuesta == null || textoRespuesta.isBlank() || textoRespuesta.length() > 255) {
                throw new ReglaNegocioException("Para el penal enviá una respuesta numérica válida");
            }
            try {
                correcta = preguntaServices.validarRespuestaNumericaDePenal(preguntaId, new java.math.BigDecimal(textoRespuesta.trim()));
            } catch (NumberFormatException e) {
                throw new ReglaNegocioException("La respuesta del penal debe ser un número válido");
            }
        } else {
            throw new ReglaNegocioException("El tipo de pregunta no está soportado");
        }

        String comodinUsado = partidoComodinRepository.findByPartidoPartidoIdAndPreguntaPreguntaId(
                partidoId, preguntaId).map(PartidoComodin::getTipoComodin).orElse("ninguno");
        intentoServices.registrarIntentoDeRespuesta(partidoId, preguntaId, opcionRespuestaId,
                textoRespuesta, tiempoServidor, comodinUsado, correcta);
        actualizarMarcadorSegunRespuesta(partido, correcta, comodinUsado);
        actual.setEstado("contestada");
        actual.setRespondidaEn(ahora);
        partidoPreguntaRepository.save(actual);
        partidoRepository.save(partido);
        return partido;
    }

    @Transactional
    public PartidoPregunta usarComodin(Long usuarioId, Long partidoId, String tipoComodin) {
        Partido partido = buscarPartidoParaActualizar(partidoId);
        validarPropiedadDelPartido(usuarioId, partido);
        validarQueElPartidoEsteEnCurso(partido);
        if (!List.of("doble", "penal", "cincuenta").contains(tipoComodin)) throw new ReglaNegocioException("El comodín indicado no es válido");
        if (partidoComodinRepository.existsByPartidoPartidoIdAndTipoComodin(partidoId, tipoComodin)) {
            throw new ReglaNegocioException("Ese comodín ya fue utilizado en este partido");
        }
        PartidoPregunta actual = primeraPreguntaPendiente(partido);
        if (actual.getPreguntaIniciadaEn() == null) throw new ReglaNegocioException("Primero solicitá la pregunta actual al servidor");
        if (Duration.between(actual.getPreguntaIniciadaEn(), LocalDateTime.now()).toMillis() > TIEMPO_MAXIMO_SEGUNDOS * 1000L) {
            registrarVencimiento(partido, actual, LocalDateTime.now());
            return obtenerAsignacionActual(partido).orElse(actual);
        }
        if (partidoComodinRepository.existsByPartidoPartidoIdAndPreguntaPreguntaId(partidoId, actual.getPregunta().getPreguntaId())) {
            throw new ReglaNegocioException("Ya se utilizó un comodín en esta pregunta");
        }
        PartidoPregunta objetivo = actual;
        if ("cincuenta".equals(tipoComodin) || "doble".equals(tipoComodin)) {
            if (!"opcion_multiple".equals(actual.getPregunta().getTipoPregunta())) {
                throw new ReglaNegocioException("Este comodín solo se puede utilizar en preguntas de opción múltiple");
            }
        } else {
            if (!"opcion_multiple".equals(actual.getPregunta().getTipoPregunta())) {
                throw new ReglaNegocioException("El penal solo puede reemplazar una pregunta de opción múltiple");
            }
            Set<Long> excluidas = partidoPreguntaRepository.findByPartidoPartidoIdOrderByNumeroPreguntaAscPartidoPreguntaIdAsc(partidoId)
                    .stream().map(pp -> pp.getPregunta().getPreguntaId()).collect(java.util.stream.Collectors.toSet());
            Pregunta preguntaPenal = preguntaServices.crearPreguntaNumericaDePenal(excluidas);
            actual.setEstado("sustituida");
            actual.setRespondidaEn(LocalDateTime.now());
            partidoPreguntaRepository.save(actual);
            objetivo = new PartidoPregunta();
            objetivo.setPartido(partido);
            objetivo.setPregunta(preguntaPenal);
            objetivo.setNumeroPregunta(actual.getNumeroPregunta());
            objetivo.setEstado("pendiente");
            objetivo.setPreguntaIniciadaEn(LocalDateTime.now());
            objetivo = partidoPreguntaRepository.save(objetivo);
        }
        PartidoComodin uso = new PartidoComodin();
        uso.setPartido(partido);
        uso.setPregunta(objetivo.getPregunta());
        uso.setTipoComodin(tipoComodin);
        if ("cincuenta".equals(tipoComodin)) {
            List<OpcionRespuesta> opciones = opcionServices.listarOpcionesDePregunta(objetivo.getPregunta().getPreguntaId());
            List<OpcionRespuesta> distractores = opciones.stream().filter(o -> !Boolean.TRUE.equals(o.getEsCorrecta())).toList();
            long cantidadCorrectas = opciones.stream().filter(o -> Boolean.TRUE.equals(o.getEsCorrecta())).count();
            if (cantidadCorrectas != 1 || distractores.isEmpty()) {
                throw new ReglaNegocioException("La pregunta no tiene una única respuesta correcta y distractores para el comodín");
            }
            uso.setOpcionDistractor(distractores.get(ThreadLocalRandom.current().nextInt(distractores.size())));
        }
        partidoComodinRepository.save(uso);
        return objetivo;
    }

    @Transactional
    public Partido finalizarPartido(Long usuarioId, Long partidoId) {
        Partido partido = buscarPartidoParaActualizar(partidoId);
        validarPropiedadDelPartido(usuarioId, partido);
        validarQueElPartidoEsteEnCurso(partido);
        LocalDateTime finalizado = LocalDateTime.now();
        partido.setEstado("finalizado");
        partido.setFinalizadoEn(finalizado);
        long duracion = Math.max(0, Duration.between(partido.getIniciadoEn(), finalizado).getSeconds());
        partido.setDuracionSegundos(Math.toIntExact(Math.min(duracion, Integer.MAX_VALUE)));
        partidoRepository.save(partido);
        carreraServices.aplicarExperienciaPorResultado(partidoId);
        temporadaServices.registrarResultado(partido);
        torneoServices.registrarResultado(partido);
        return partido;
    }

    public Partido obtenerEstadoActualDelPartido(Long usuarioId, Long partidoId) {
        Partido partido = buscarPartido(partidoId);
        validarPropiedadDelPartido(usuarioId, partido);
        return partido;
    }

    Partido buscarPartido(Long partidoId) {
        if (partidoId == null) throw new ReglaNegocioException("El ID del partido es obligatorio");
        return partidoRepository.findById(partidoId).orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el partido"));
    }

    private Partido buscarPartidoParaActualizar(Long partidoId) {
        if (partidoId == null) throw new ReglaNegocioException("El ID del partido es obligatorio");
        return partidoRepository.buscarParaActualizar(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el partido"));
    }

    private Optional<PartidoPregunta> obtenerAsignacionActual(Partido partido) {
        while (true) {
            List<PartidoPregunta> pendientes = partidoPreguntaRepository.buscarPendientesParaActualizar(partido.getPartidoId(), "pendiente");
            if (pendientes.isEmpty()) return Optional.empty();
            PartidoPregunta actual = pendientes.getFirst();
            LocalDateTime ahora = LocalDateTime.now();
            if (actual.getPreguntaIniciadaEn() == null) {
                actual.setPreguntaIniciadaEn(ahora);
                return Optional.of(partidoPreguntaRepository.save(actual));
            }
            long milisegundos = Duration.between(actual.getPreguntaIniciadaEn(), ahora).toMillis();
            if (milisegundos <= TIEMPO_MAXIMO_SEGUNDOS * 1000L) return Optional.of(actual);
            registrarVencimiento(partido, actual, ahora);
        }
    }

    private PartidoPregunta primeraPreguntaPendiente(Partido partido) {
        List<PartidoPregunta> pendientes = partidoPreguntaRepository.buscarPendientesParaActualizar(partido.getPartidoId(), "pendiente");
        if (pendientes.isEmpty()) throw new RecursoNoEncontradoException("El partido ya no tiene preguntas pendientes");
        return pendientes.getFirst();
    }

    private void registrarVencimiento(Partido partido, PartidoPregunta pregunta, LocalDateTime ahora) {
        intentoServices.registrarIntentoDeRespuesta(partido.getPartidoId(), pregunta.getPregunta().getPreguntaId(),
                null, null, TIEMPO_MAXIMO_SEGUNDOS, "ninguno", false);
        actualizarMarcadorSegunRespuesta(partido, false, "ninguno");
        pregunta.setEstado("contestada");
        pregunta.setRespondidaEn(ahora);
        partidoPreguntaRepository.save(pregunta);
        partidoRepository.save(partido);
    }

    private int validarTiempoDeRespuesta(Integer segundosInformados, LocalDateTime preguntaIniciada, LocalDateTime recibidaEn) {
        if (segundosInformados == null || segundosInformados < 0 || segundosInformados > TIEMPO_MAXIMO_SEGUNDOS) {
            throw new ReglaNegocioException("El tiempo informado no es válido");
        }
        long msServidor = Duration.between(preguntaIniciada, recibidaEn).toMillis();
        if (msServidor < 0 || msServidor > TIEMPO_MAXIMO_SEGUNDOS * 1000L) {
            throw new ReglaNegocioException("El tiempo para responder la pregunta expiró");
        }
        return (int) Math.min(TIEMPO_MAXIMO_SEGUNDOS, (msServidor + 999) / 1000);
    }

    private void actualizarMarcadorSegunRespuesta(Partido partido, boolean correcta, String comodin) {
        if (correcta) {
            partido.setRespuestasCorrectas(partido.getRespuestasCorrectas() + 1);
            int goles = "doble".equals(comodin) ? 2 : 1;
            partido.setGolesLocal(partido.getGolesLocal() + goles);
        } else {
            partido.setRespuestasIncorrectas(partido.getRespuestasIncorrectas() + 1);
            partido.setGolesVisitante(partido.getGolesVisitante() + 1);
        }
    }

    private void validarQueElPartidoEsteEnCurso(Partido partido) {
        if (partido == null || !"en_curso".equals(partido.getEstado())) throw new ReglaNegocioException("El partido no está en curso");
    }

    private void validarPropiedadDelPartido(Long usuarioId, Partido partido) {
        carreraServices.validarQueLaCarreraPertenezcaAlUsuario(partido.getCarrera().getCarreraId(), usuarioId);
    }
}
