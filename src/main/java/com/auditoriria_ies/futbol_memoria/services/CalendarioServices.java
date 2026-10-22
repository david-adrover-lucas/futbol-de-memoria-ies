package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.models.*;
import com.auditoriria_ies.futbol_memoria.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class CalendarioServices {
    private final CarreraServices carreraServices;
    private final CarreraTemporadaRepository temporadaRepository;
    private final CalendarioFechaRepository calendarioRepository;
    private final CompeticionRepository competicionRepository;
    private final ClasificacionServices clasificacionServices;
    private final CarreraTorneoRepository torneoRepository;
    private final CarreraTorneoServices torneoServices;

    public CalendarioServices(CarreraServices carreraServices, CarreraTemporadaRepository temporadaRepository,
                             CalendarioFechaRepository calendarioRepository, CompeticionRepository competicionRepository,
                             ClasificacionServices clasificacionServices, CarreraTorneoRepository torneoRepository,
                             CarreraTorneoServices torneoServices) {
        this.carreraServices = carreraServices; this.temporadaRepository = temporadaRepository;
        this.calendarioRepository = calendarioRepository; this.competicionRepository = competicionRepository;
        this.clasificacionServices = clasificacionServices; this.torneoRepository = torneoRepository;
        this.torneoServices = torneoServices;
    }

    public record FechaVista(Integer numeroFecha, String codigoCompeticion, String nombreCompeticion,
                             Integer numeroRonda, String nombreRonda, String estado,
                             boolean participaJugador, boolean esFechaLibre) {}
    public record CalendarioVista(Integer numeroTemporada, Integer fechaActual, Integer totalFechas,
                                  String codigoCompeticionActual, String nombreCompeticionActual,
                                  Integer rondaActual, String nombreRondaActual, boolean participaJugador,
                                  boolean esFechaLibre, List<FechaVista> fechas) {}

    @Transactional
    public CalendarioVista obtenerCalendarioActual(Long usuarioId) {
        Carrera carrera = carreraServices.obtenerCarreraActivaPorUsuario(usuarioId);
        CarreraTemporada temporada = obtenerOCrearTemporada(carrera);
        asegurarFechas(temporada);
        Optional<CalendarioFecha> actual = calendarioRepository.findFirstByTemporadaCarreraTemporadaIdAndEstadoOrderByNumeroFechaAsc(
                temporada.getCarreraTemporadaId(), "pendiente");
        if (actual.isEmpty() && "finalizada".equals(temporada.getEstado())) {
            carrera.setNumeroTemporada(carrera.getNumeroTemporada() + 1);
            carreraServices.guardarCarrera(carrera);
            temporada = obtenerOCrearTemporada(carrera);
            asegurarFechas(temporada);
            actual = calendarioRepository.findFirstByTemporadaCarreraTemporadaIdAndEstadoOrderByNumeroFechaAsc(
                    temporada.getCarreraTemporadaId(), "pendiente");
        }
        if (actual.isEmpty()) throw new ReglaNegocioException("No quedan fechas pendientes en la temporada");
        return vista(carrera, temporada, actual.get());
    }

    @Transactional
    public CalendarioVista avanzarFechaLibre(Long usuarioId) {
        Carrera carrera = carreraServices.obtenerCarreraActivaPorUsuario(usuarioId);
        CarreraTemporada temporada = obtenerOCrearTemporada(carrera);
        asegurarFechas(temporada);
        CalendarioFecha fecha = calendarioRepository.findFirstByTemporadaCarreraTemporadaIdAndEstadoOrderByNumeroFechaAsc(
                        temporada.getCarreraTemporadaId(), "pendiente")
                .orElseThrow(() -> new ReglaNegocioException("No quedan fechas pendientes en la temporada"));
        boolean participa = participa(carrera, fecha);
        if (participa) throw new ReglaNegocioException("Esta fecha tiene un partido de tu equipo; jugalo antes de avanzar");
        String codigo = fecha.getCompeticion().getCodigo();
        if (!"temporada".equals(codigo)) torneoServices.simularRonda(usuarioId, temporada.getNumeroTemporada(), codigo, fecha.getNumeroRonda());
        fecha.setEstado("completada"); fecha.setCompletadaEn(LocalDateTime.now()); calendarioRepository.save(fecha);
        return obtenerCalendarioActual(usuarioId);
    }

    @Transactional
    public void completarFechaDeLiga(Partido partido) {
        if (partido.getTemporadaFixture() == null) return;
        CarreraTemporada temporada = partido.getTemporadaFixture().getTemporada();
        completar(temporada.getCarreraTemporadaId(), "temporada", partido.getTemporadaFixture().getJornada());
    }

    private void completar(Long temporadaId, String codigo, Integer ronda) {
        calendarioRepository.findByTemporadaCarreraTemporadaIdAndCompeticionCodigoAndNumeroRonda(temporadaId, codigo, ronda)
                .ifPresent(fecha -> {
                    fecha.setEstado("completada"); fecha.setCompletadaEn(LocalDateTime.now()); calendarioRepository.save(fecha);
                });
    }

    private CarreraTemporada obtenerOCrearTemporada(Carrera carrera) {
        return temporadaRepository.findByCarreraCarreraIdAndNumeroTemporada(carrera.getCarreraId(), carrera.getNumeroTemporada())
                .orElseGet(() -> {
                    CarreraTemporada nueva = new CarreraTemporada(); nueva.setCarrera(carrera);
                    nueva.setNumeroTemporada(carrera.getNumeroTemporada()); nueva.setJornadaActual(1);
                    return temporadaRepository.save(nueva);
                });
    }

    private void asegurarFechas(CarreraTemporada temporada) {
        List<CalendarioFecha> existentes = calendarioRepository.findByTemporadaCarreraTemporadaIdOrderByNumeroFechaAsc(
                temporada.getCarreraTemporadaId());
        if (existentes.size() == ProgramacionTemporada.TOTAL_FECHAS) return;
        Map<String, Competicion> competiciones = new HashMap<>();
        competicionRepository.findAll().forEach(c -> competiciones.put(c.getCodigo(), c));
        Set<Integer> fechasGuardadas = new HashSet<>(); existentes.forEach(f -> fechasGuardadas.add(f.getNumeroFecha()));
        List<CalendarioFecha> nuevas = new ArrayList<>();
        for (ProgramacionTemporada.Evento evento : ProgramacionTemporada.eventos()) {
            if (fechasGuardadas.contains(evento.fecha())) continue;
            Competicion competicion = competiciones.get(evento.competicion());
            if (competicion == null) throw new RecursoNoEncontradoException("Falta la competición " + evento.competicion());
            CalendarioFecha fecha = new CalendarioFecha(); fecha.setTemporada(temporada);
            fecha.setNumeroFecha(evento.fecha()); fecha.setCompeticion(competicion); fecha.setNumeroRonda(evento.ronda());
            nuevas.add(fecha);
        }
        calendarioRepository.saveAll(nuevas);
    }

    private boolean participa(Carrera carrera, CalendarioFecha fecha) {
        String codigo = fecha.getCompeticion().getCodigo();
        if ("temporada".equals(codigo) || "nacional".equals(codigo)) {
            Optional<CarreraTorneo> torneo = torneoRepository.findByCarreraCarreraIdAndNumeroTemporadaAndCodigoCompeticion(
                    carrera.getCarreraId(), carrera.getNumeroTemporada(), codigo);
            return torneo.isEmpty() || "en_curso".equals(torneo.get().getEstado());
        }
        if (!clasificacionServices.estaClasificadoParaTemporada(carrera.getCarreraId(), carrera.getNumeroTemporada(),
                carrera.getEquipoActual().getEquipoId(), codigo)) return false;
        return torneoRepository.findByCarreraCarreraIdAndNumeroTemporadaAndCodigoCompeticion(
                        carrera.getCarreraId(), carrera.getNumeroTemporada(), codigo)
                .map(t -> "en_curso".equals(t.getEstado())).orElse(true);
    }

    private CalendarioVista vista(Carrera carrera, CarreraTemporada temporada, CalendarioFecha actual) {
        Map<String, Competicion> competiciones = new HashMap<>();
        competicionRepository.findAll().forEach(c -> competiciones.put(c.getCodigo(), c));
        List<FechaVista> fechas = calendarioRepository.findByTemporadaCarreraTemporadaIdOrderByNumeroFechaAsc(
                        temporada.getCarreraTemporadaId()).stream()
                .map(fecha -> {
                    boolean participa = participa(carrera, fecha);
                    return new FechaVista(fecha.getNumeroFecha(), fecha.getCompeticion().getCodigo(),
                            fecha.getCompeticion().getNombre(), fecha.getNumeroRonda(), nombreRonda(fecha.getCompeticion().getCodigo(), fecha.getNumeroRonda()),
                            fecha.getEstado(), participa, "pendiente".equals(fecha.getEstado()) && !participa);
                }).toList();
        boolean participaActual = participa(carrera, actual);
        return new CalendarioVista(temporada.getNumeroTemporada(), actual.getNumeroFecha(), ProgramacionTemporada.TOTAL_FECHAS,
                actual.getCompeticion().getCodigo(), actual.getCompeticion().getNombre(), actual.getNumeroRonda(),
                nombreRonda(actual.getCompeticion().getCodigo(), actual.getNumeroRonda()), participaActual, !participaActual, fechas);
    }

    private String nombreRonda(String codigo, Integer ronda) {
        if ("temporada".equals(codigo)) return "Jornada " + ronda;
        return switch (ronda) {
            case 1 -> "Octavos";
            case 2 -> "Cuartos";
            case 3 -> "Semifinal";
            case 4 -> "Final";
            default -> "Ronda " + ronda;
        };
    }
}
