package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.models.*;
import com.auditoriria_ies.futbol_memoria.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class TandaPenalesServices {
    private static final int TIEMPO_MAXIMO_SEGUNDOS = 15;
    private final TandaPenalesRepository tandaRepository;
    private final TandaPenalTurnoRepository turnoRepository;
    private final PartidoRepository partidoRepository;
    private final PreguntaRepository preguntaRepository;
    private final PreguntaServices preguntaServices;
    private final CarreraServices carreraServices;
    private final CarreraTorneoServices torneoServices;

    public TandaPenalesServices(TandaPenalesRepository tandaRepository, TandaPenalTurnoRepository turnoRepository,
                                PartidoRepository partidoRepository, PreguntaRepository preguntaRepository,
                                PreguntaServices preguntaServices, CarreraServices carreraServices,
                                CarreraTorneoServices torneoServices) {
        this.tandaRepository = tandaRepository; this.turnoRepository = turnoRepository;
        this.partidoRepository = partidoRepository; this.preguntaRepository = preguntaRepository;
        this.preguntaServices = preguntaServices; this.carreraServices = carreraServices;
        this.torneoServices = torneoServices;
    }

    public record OpcionPenal(Long opcionRespuestaId, Integer ordenOpcion, String textoOpcion) {}
    public record PreguntaPenal(Long preguntaId, String textoPregunta, String tipoPregunta,
                                LocalDateTime preguntaIniciadaEn, List<OpcionPenal> opciones) {}
    public record TurnoPenalVista(Integer numeroTurno, Integer numeroRonda, String fase, String rolJugador,
                                  Boolean correcta, Integer golesJugador, Integer golesRival,
                                  Long preguntaId, LocalDateTime respondidaEn) {}
    public record TandaPenalVista(Long partidoId, String estado, String faseActual, Integer numeroTurnoActual,
                                  Integer golesJugador, Integer golesRival, Long equipoGanadorId,
                                  String nombreGanador, PreguntaPenal preguntaActual,
                                  List<TurnoPenalVista> turnos) {}

    @Transactional
    public TandaPenalVista iniciarObtener(Long usuarioId, Long partidoId) {
        Partido partido = partidoRepository.buscarParaActualizar(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el partido"));
        validarPropiedad(usuarioId, partido);
        TandaPenales existente = tandaRepository.findByPartidoPartidoId(partidoId).orElse(null);
        if (existente != null && "finalizada".equals(existente.getEstado())) return vista(existente);
        validarPartidoEliminatorioEmpatado(partido);
        if (existente == null && preguntaRepository.findByEstaActivaTrue().stream()
                .filter(p -> List.of("opcion_multiple", "penal_numerico").contains(p.getTipoPregunta())).count()
                < TandaPenalesRules.TURNOS_REGLAMENTARIOS) {
            throw new ReglaNegocioException("Se necesitan al menos 10 preguntas activas para comenzar la tanda");
        }
        TandaPenales tanda = existente != null ? existente : crearTanda(partido);
        if ("en_curso".equals(tanda.getEstado())) {
            procesarVencimientoSiCorresponde(tanda);
            if ("en_curso".equals(tanda.getEstado())) asegurarTurnoActual(tanda);
        }
        return vista(tanda);
    }

    @Transactional
    public TandaPenalVista obtener(Long usuarioId, Long partidoId) {
        return iniciarObtener(usuarioId, partidoId);
    }

    @Transactional
    public TandaPenalVista responder(Long usuarioId, Long partidoId, Long preguntaId, Long opcionId,
                                     String textoRespuesta, Integer segundosInformados) {
        Partido partido = partidoRepository.buscarParaActualizar(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el partido"));
        validarPropiedad(usuarioId, partido);
        TandaPenales tanda = tandaRepository.buscarParaActualizar(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se inició la tanda de penales"));
        if (!"en_curso".equals(tanda.getEstado())) return vista(tanda);
        TandaPenalTurno turno = asegurarTurnoActual(tanda);
        if (turno == null) throw new ReglaNegocioException("No quedan preguntas activas sin repetir para continuar la tanda");
        if (!turno.getPregunta().getPreguntaId().equals(preguntaId)) {
            throw new ReglaNegocioException("La respuesta no corresponde a la pregunta actual de los penales");
        }
        LocalDateTime ahora = LocalDateTime.now();
        long transcurrido = Duration.between(turno.getPreguntaIniciadaEn(), ahora).toMillis();
        if (transcurrido > TIEMPO_MAXIMO_SEGUNDOS * 1000L) {
            registrarRespuesta(tanda, turno, false, null, null, ahora);
            avanzar(tanda);
            return vista(tanda);
        }
        if (segundosInformados == null || segundosInformados < 0 || segundosInformados > TIEMPO_MAXIMO_SEGUNDOS) {
            throw new ReglaNegocioException("El tiempo informado no es válido");
        }
        if ("opcion_multiple".equals(turno.getPregunta().getTipoPregunta())) {
            if (opcionId == null || textoRespuesta != null) throw new ReglaNegocioException("Seleccioná una opción para responder");
            boolean correcta = preguntaServices.validarRespuestaSeleccionada(preguntaId, opcionId);
            OpcionRespuesta opcion = turno.getPregunta().getOpciones().stream()
                    .filter(o -> o.getOpcionRespuestaId().equals(opcionId)).findFirst()
                    .orElseThrow(() -> new ReglaNegocioException("La opción no pertenece a la pregunta actual"));
            registrarRespuesta(tanda, turno, correcta, opcion, null, ahora);
        } else if ("penal_numerico".equals(turno.getPregunta().getTipoPregunta())) {
            if (opcionId != null || textoRespuesta == null || textoRespuesta.isBlank() || textoRespuesta.length() > 255) {
                throw new ReglaNegocioException("Ingresá una respuesta numérica válida");
            }
            boolean correcta;
            try { correcta = preguntaServices.validarRespuestaNumericaDePenal(preguntaId, new BigDecimal(textoRespuesta.trim())); }
            catch (NumberFormatException e) { throw new ReglaNegocioException("La respuesta debe ser un número válido"); }
            registrarRespuesta(tanda, turno, correcta, null, textoRespuesta.trim(), ahora);
        } else throw new ReglaNegocioException("El tipo de pregunta de penal no está soportado");
        avanzar(tanda);
        return vista(tanda);
    }

    private void validarPartidoEliminatorioEmpatado(Partido partido) {
        if (!"finalizado".equals(partido.getEstado()) || partido.getTorneoFixture() == null
                || !"pendiente_penales".equals(partido.getTorneoFixture().getEstado())
                || !Objects.equals(partido.getGolesLocal(), partido.getGolesVisitante())) {
            throw new ReglaNegocioException("Este partido no está empatado en una eliminatoria que requiera penales");
        }
    }

    private TandaPenales crearTanda(Partido partido) {
        TandaPenales nueva = new TandaPenales(); nueva.setPartido(partido);
        return tandaRepository.save(nueva);
    }

    private void validarPropiedad(Long usuarioId, Partido partido) {
        carreraServices.validarQueLaCarreraPertenezcaAlUsuario(partido.getCarrera().getCarreraId(), usuarioId);
    }

    private TandaPenalTurno asegurarTurnoActual(TandaPenales tanda) {
        return turnoRepository.findByTandaTandaPenalesIdAndNumeroTurno(tanda.getTandaPenalesId(), tanda.getNumeroTurnoActual())
                .orElseGet(() -> {
                    Set<Long> usadas = new HashSet<>();
                    turnoRepository.findByTandaTandaPenalesIdOrderByNumeroTurnoAsc(tanda.getTandaPenalesId())
                            .forEach(t -> usadas.add(t.getPregunta().getPreguntaId()));
                    List<Pregunta> candidatas = preguntaRepository.findByEstaActivaTrue().stream()
                            .filter(p -> !usadas.contains(p.getPreguntaId()))
                            .filter(p -> List.of("opcion_multiple", "penal_numerico").contains(p.getTipoPregunta()))
                            .toList();
                    if (candidatas.isEmpty()) return null;
                    Pregunta pregunta = candidatas.get(new Random().nextInt(candidatas.size()));
                    TandaPenalTurno turno = new TandaPenalTurno(); turno.setTanda(tanda); turno.setPregunta(pregunta);
                    int numero = tanda.getNumeroTurnoActual();
                    turno.setNumeroTurno(numero); turno.setFase(TandaPenalesRules.fase(numero));
                    turno.setNumeroRonda(TandaPenalesRules.numeroRonda(numero));
                    turno.setRolJugador(TandaPenalesRules.rolJugador(numero));
                    turno.setPreguntaIniciadaEn(LocalDateTime.now());
                    tanda.setFaseActual(turno.getFase());
                    return turnoRepository.save(turno);
                });
    }

    private void procesarVencimientoSiCorresponde(TandaPenales tanda) {
        TandaPenalTurno turno = turnoRepository.findByTandaTandaPenalesIdAndNumeroTurno(tanda.getTandaPenalesId(), tanda.getNumeroTurnoActual()).orElse(null);
        if (turno != null && turno.getRespondidaEn() == null
                && Duration.between(turno.getPreguntaIniciadaEn(), LocalDateTime.now()).toMillis() > TIEMPO_MAXIMO_SEGUNDOS * 1000L) {
            registrarRespuesta(tanda, turno, false, null, null, LocalDateTime.now());
            avanzar(tanda);
        }
    }

    private void registrarRespuesta(TandaPenales tanda, TandaPenalTurno turno, boolean correcta,
                                    OpcionRespuesta opcion, String texto, LocalDateTime ahora) {
        turno.setCorrecta(correcta); turno.setOpcionRespuesta(opcion); turno.setTextoRespuesta(texto); turno.setRespondidaEn(ahora);
        int jugador = 0, rival = 0;
        if ("patea".equals(turno.getRolJugador()) && correcta) jugador = 1;
        if ("ataja".equals(turno.getRolJugador()) && !correcta) rival = 1;
        turno.setGolesJugadorGenerados(jugador); turno.setGolesRivalGenerados(rival);
        tanda.setGolesJugador(tanda.getGolesJugador() + jugador); tanda.setGolesRival(tanda.getGolesRival() + rival);
        turnoRepository.save(turno); tandaRepository.save(tanda);
    }

    private void avanzar(TandaPenales tanda) {
        int respondidos = tanda.getNumeroTurnoActual();
        if (respondidos == TandaPenalesRules.TURNOS_REGLAMENTARIOS) {
            if (TandaPenalesRules.tieneGanadorTrasTurno(respondidos, tanda.getGolesJugador(), tanda.getGolesRival())) { finalizar(tanda); return; }
            tanda.setFaseActual("muerte_subita");
        } else if (TandaPenalesRules.tieneGanadorTrasTurno(respondidos, tanda.getGolesJugador(), tanda.getGolesRival())) {
            finalizar(tanda); return;
        }
        tanda.setNumeroTurnoActual(respondidos + 1);
        tandaRepository.save(tanda);
        asegurarTurnoActual(tanda);
    }

    private void finalizar(TandaPenales tanda) {
        Equipo ganador = tanda.getGolesJugador() > tanda.getGolesRival()
                ? tanda.getPartido().getEquipoLocal() : tanda.getPartido().getEquipoVisitante();
        tanda.setEstado("finalizada"); tanda.setEquipoGanador(ganador); tanda.setFinalizadaEn(LocalDateTime.now());
        tandaRepository.save(tanda);
        torneoServices.registrarGanadorPorPenales(tanda.getPartido().getPartidoId(), ganador.getEquipoId());
    }

    private TandaPenalVista vista(TandaPenales tanda) {
        PreguntaPenal preguntaActual = null;
        if ("en_curso".equals(tanda.getEstado())) {
            TandaPenalTurno turno = asegurarTurnoActual(tanda);
            if (turno != null) {
                Pregunta p = preguntaServices.obtenerPreguntaConOpcionesParaPartido(turno.getPregunta().getPreguntaId());
                List<OpcionPenal> opciones = "opcion_multiple".equals(p.getTipoPregunta()) ? p.getOpciones().stream()
                        .map(o -> new OpcionPenal(o.getOpcionRespuestaId(), o.getOrdenOpcion(), o.getTextoOpcion())).toList() : List.of();
                preguntaActual = new PreguntaPenal(p.getPreguntaId(), p.getTextoPregunta(), p.getTipoPregunta(), turno.getPreguntaIniciadaEn(), opciones);
            }
        }
        List<TurnoPenalVista> turnos = turnoRepository.findByTandaTandaPenalesIdOrderByNumeroTurnoAsc(tanda.getTandaPenalesId()).stream()
                .map(t -> new TurnoPenalVista(t.getNumeroTurno(), t.getNumeroRonda(), t.getFase(), t.getRolJugador(),
                        t.getCorrecta(), t.getGolesJugadorGenerados(), t.getGolesRivalGenerados(),
                        t.getPregunta().getPreguntaId(), t.getRespondidaEn())).toList();
        return new TandaPenalVista(tanda.getPartido().getPartidoId(), tanda.getEstado(), tanda.getFaseActual(),
                tanda.getNumeroTurnoActual(), tanda.getGolesJugador(), tanda.getGolesRival(),
                tanda.getEquipoGanador() == null ? null : tanda.getEquipoGanador().getEquipoId(),
                tanda.getEquipoGanador() == null ? null : tanda.getEquipoGanador().getNombre(), preguntaActual, turnos);
    }
}
