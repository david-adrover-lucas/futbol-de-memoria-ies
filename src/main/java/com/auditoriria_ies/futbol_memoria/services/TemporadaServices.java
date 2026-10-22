package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.models.*;
import com.auditoriria_ies.futbol_memoria.repository.CarreraTemporadaRepository;
import com.auditoriria_ies.futbol_memoria.repository.EquipoRepository;
import com.auditoriria_ies.futbol_memoria.repository.TemporadaFixtureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional(readOnly = true)
public class TemporadaServices {
    private static final int JORNADAS = 29;
    private final CarreraServices carreraServices;
    private final CarreraTemporadaRepository temporadaRepository;
    private final TemporadaFixtureRepository fixtureRepository;
    private final EquipoRepository equipoRepository;

    public TemporadaServices(CarreraServices carreraServices, CarreraTemporadaRepository temporadaRepository,
                             TemporadaFixtureRepository fixtureRepository, EquipoRepository equipoRepository) {
        this.carreraServices = carreraServices;
        this.temporadaRepository = temporadaRepository;
        this.fixtureRepository = fixtureRepository;
        this.equipoRepository = equipoRepository;
    }

    public record EquipoVista(Long equipoId, String codigoExterno, String nombre, String apodo, Integer prestigio, String rutaEscudo) {}
    public record FixtureVista(Long fixtureId, Integer jornada, Integer numeroPartido, EquipoVista equipoLocal,
                               EquipoVista equipoVisitante, Integer golesLocal, Integer golesVisitante,
                               String estado, Long partidoId, boolean esPartidoDelUsuario) {}
    public record PosicionVista(Integer posicion, EquipoVista equipo, long jugados, long ganados, long empatados,
                                long perdidos, long golesFavor, long golesContra, long diferencia, long puntos) {}
    public record TemporadaVista(Long temporadaId, Integer numeroTemporada, Integer jornadaActual,
                                 Integer jornadasTotales, List<FixtureVista> fixtures, List<PosicionVista> tabla) {}

    @Transactional
    public TemporadaVista obtenerTemporada(Long usuarioId) {
        Carrera carrera = carreraServices.obtenerCarreraActivaPorUsuario(usuarioId);
        CarreraTemporada temporada = buscarOCrearTemporada(carrera);
        List<TemporadaFixture> jornada = asegurarJornada(temporada);
        return vista(temporada, jornada);
    }

    @Transactional
    public FixtureVista obtenerPartidoSiguiente(Long usuarioId) {
        Carrera carrera = carreraServices.obtenerCarreraActivaPorUsuario(usuarioId);
        CarreraTemporada temporada = buscarOCrearTemporada(carrera);
        List<TemporadaFixture> jornada = asegurarJornada(temporada);
        TemporadaFixture fixture = jornada.stream()
                .filter(f -> f.getEquipoLocal().getEquipoId().equals(carrera.getEquipoActual().getEquipoId())
                        || f.getEquipoVisitante().getEquipoId().equals(carrera.getEquipoActual().getEquipoId()))
                .findFirst().orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el partido de tu equipo en esta jornada"));
        if ("finalizado".equals(fixture.getEstado())) {
            if (temporada.getJornadaActual() < JORNADAS) {
                temporada.setJornadaActual(temporada.getJornadaActual() + 1);
                temporada = temporadaRepository.save(temporada);
            } else {
                temporada.setEstado("finalizada");
                temporadaRepository.save(temporada);
                carrera.setNumeroTemporada(carrera.getNumeroTemporada() + 1);
                carreraServices.guardarCarrera(carrera);
                temporada = buscarOCrearTemporada(carrera);
            }
            jornada = asegurarJornada(temporada);
            fixture = jornada.stream().filter(f -> f.getEquipoLocal().getEquipoId().equals(carrera.getEquipoActual().getEquipoId())
                    || f.getEquipoVisitante().getEquipoId().equals(carrera.getEquipoActual().getEquipoId())).findFirst()
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el próximo partido de temporada"));
        }
        return fixtureVista(fixture, carrera.getEquipoActual().getEquipoId());
    }

    @Transactional
    public TemporadaFixture validarYObtenerFixture(Long fixtureId, Long usuarioId) {
        Carrera carrera = carreraServices.obtenerCarreraActivaPorUsuario(usuarioId);
        TemporadaFixture fixture = fixtureRepository.findByTemporadaFixtureIdAndTemporadaCarreraCarreraId(fixtureId, carrera.getCarreraId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ese partido de temporada"));
        if (!"pendiente".equals(fixture.getEstado()) || fixture.getPartido() != null) throw new ReglaNegocioException("El partido de temporada ya fue iniciado o finalizado");
        if (!Objects.equals(fixture.getJornada(), fixture.getTemporada().getJornadaActual())) throw new ReglaNegocioException("El fixture no pertenece a la jornada actual");
        if (!Objects.equals(fixture.getEquipoLocal().getEquipoId(), carrera.getEquipoActual().getEquipoId())) {
            throw new ReglaNegocioException("El fixture no corresponde al equipo actual de la carrera");
        }
        return fixture;
    }

    @Transactional
    public void asignarFixture(TemporadaFixture fixture, Partido partido) {
        fixture.setPartido(partido);
        fixtureRepository.save(fixture);
    }

    @Transactional
    public void registrarResultado(Partido partido) {
        fixtureRepository.findByPartidoPartidoId(partido.getPartidoId()).ifPresent(fixture -> {
            fixture.setGolesLocal(partido.getGolesLocal());
            fixture.setGolesVisitante(partido.getGolesVisitante());
            fixture.setEstado("finalizado");
            fixtureRepository.save(fixture);
        });
    }

    private CarreraTemporada buscarOCrearTemporada(Carrera carrera) {
        return temporadaRepository.findByCarreraCarreraIdAndNumeroTemporada(carrera.getCarreraId(), carrera.getNumeroTemporada())
                .orElseGet(() -> {
                    CarreraTemporada nueva = new CarreraTemporada();
                    nueva.setCarrera(carrera);
                    nueva.setNumeroTemporada(carrera.getNumeroTemporada());
                    nueva.setJornadaActual(1);
                    return temporadaRepository.save(nueva);
                });
    }

    private List<TemporadaFixture> asegurarJornada(CarreraTemporada temporada) {
        List<TemporadaFixture> jornada = fixtureRepository.findByTemporadaCarreraTemporadaIdAndJornadaOrderByNumeroPartido(
                temporada.getCarreraTemporadaId(), temporada.getJornadaActual());
        if (!jornada.isEmpty()) return jornada;
        List<Equipo> equipos = new ArrayList<>(equipoRepository.findByPaisOrderByPrestigioDesc("Argentina"));
        Equipo actual = temporada.getCarrera().getEquipoActual();
        if (actual != null && equipos.stream().noneMatch(e -> e.getEquipoId().equals(actual.getEquipoId()))) {
            if (!equipos.isEmpty()) equipos.remove(equipos.size() - 1);
            equipos.add(actual);
        }
        if (equipos.size() < 4 || equipos.size() % 2 != 0) throw new ReglaNegocioException("La temporada necesita un número par de equipos argentinos (mínimo 4)");
        equipos.sort(Comparator.comparing(Equipo::getEquipoId));
        int n = equipos.size();
        for (int turno = 1; turno < temporada.getJornadaActual(); turno++) {
            Equipo ultimo = equipos.remove(n - 1);
            equipos.add(1, ultimo);
        }
        Long equipoUsuario = temporada.getCarrera().getEquipoActual().getEquipoId();
        List<TemporadaFixture> nuevos = new ArrayList<>();
        for (int index = 0; index < n / 2; index++) {
            Equipo local = equipos.get(index);
            Equipo visitante = equipos.get(n - 1 - index);
            if (visitante.getEquipoId().equals(equipoUsuario)) { Equipo swap = local; local = visitante; visitante = swap; }
            TemporadaFixture fixture = new TemporadaFixture();
            fixture.setTemporada(temporada); fixture.setJornada(temporada.getJornadaActual());
            fixture.setNumeroPartido(index + 1); fixture.setEquipoLocal(local); fixture.setEquipoVisitante(visitante);
            if (!local.getEquipoId().equals(equipoUsuario)) {
                fixture.setGolesLocal(ThreadLocalRandom.current().nextInt(5));
                fixture.setGolesVisitante(ThreadLocalRandom.current().nextInt(5));
                fixture.setEstado("finalizado");
            }
            nuevos.add(fixture);
        }
        return fixtureRepository.saveAll(nuevos);
    }

    private TemporadaVista vista(CarreraTemporada temporada, List<TemporadaFixture> jornada) {
        List<TemporadaFixture> todos = fixtureRepository.findByTemporadaCarreraTemporadaIdOrderByJornadaAscNumeroPartidoAsc(temporada.getCarreraTemporadaId());
        Map<Long, long[]> table = new HashMap<>();
        List<Equipo> participantes = new ArrayList<>(equipoRepository.findByPaisOrderByPrestigioDesc("Argentina"));
        Equipo actual = temporada.getCarrera().getEquipoActual();
        if (actual != null && participantes.stream().noneMatch(e -> e.getEquipoId().equals(actual.getEquipoId()))) {
            if (!participantes.isEmpty()) participantes.remove(participantes.size() - 1);
            participantes.add(actual);
        }
        for (Equipo equipo : participantes) table.put(equipo.getEquipoId(), new long[8]);
        for (TemporadaFixture fixture : todos) if ("finalizado".equals(fixture.getEstado())) {
            long[] local = table.get(fixture.getEquipoLocal().getEquipoId());
            long[] away = table.get(fixture.getEquipoVisitante().getEquipoId());
            if (local == null || away == null) continue;
            int gl = fixture.getGolesLocal(), gv = fixture.getGolesVisitante();
            local[0]++; away[0]++; local[4] += gl; local[5] += gv; away[4] += gv; away[5] += gl;
            if (gl > gv) { local[1]++; away[3]++; local[7] += 3; }
            else if (gl < gv) { away[1]++; local[3]++; away[7] += 3; }
            else { local[2]++; away[2]++; local[7]++; away[7]++; }
        }
        List<PosicionVista> standings = new ArrayList<>();
        Map<Long, Equipo> equiposPorId = new HashMap<>();
        participantes.forEach(e -> equiposPorId.put(e.getEquipoId(), e));
        table.forEach((teamId, s) -> {
            Equipo team = equiposPorId.get(teamId);
            if (team != null) standings.add(new PosicionVista(0, equipoVista(team), s[0], s[1], s[2], s[3], s[4], s[5], s[4] - s[5], s[7]));
        });
        standings.sort(Comparator.comparingLong(PosicionVista::puntos).reversed()
                .thenComparing(Comparator.comparingLong(PosicionVista::diferencia).reversed())
                .thenComparing(Comparator.comparingLong(PosicionVista::golesFavor).reversed()));
        for (int i = 0; i < standings.size(); i++) { PosicionVista item = standings.get(i); standings.set(i, new PosicionVista(i + 1,
                item.equipo(), item.jugados(), item.ganados(), item.empatados(), item.perdidos(), item.golesFavor(), item.golesContra(), item.diferencia(), item.puntos())); }
        Long userTeam = temporada.getCarrera().getEquipoActual().getEquipoId();
        return new TemporadaVista(temporada.getCarreraTemporadaId(), temporada.getNumeroTemporada(), temporada.getJornadaActual(),
                JORNADAS, jornada.stream().map(f -> fixtureVista(f, userTeam)).toList(), standings);
    }

    private FixtureVista fixtureVista(TemporadaFixture f, Long userTeam) {
        return new FixtureVista(f.getTemporadaFixtureId(), f.getJornada(), f.getNumeroPartido(), equipoVista(f.getEquipoLocal()),
                equipoVista(f.getEquipoVisitante()), f.getGolesLocal(), f.getGolesVisitante(), f.getEstado(),
                f.getPartido() == null ? null : f.getPartido().getPartidoId(),
                f.getEquipoLocal().getEquipoId().equals(userTeam) || f.getEquipoVisitante().getEquipoId().equals(userTeam));
    }

    private EquipoVista equipoVista(Equipo e) { return new EquipoVista(e.getEquipoId(), e.getCodigoExterno(), e.getNombre(), e.getApodo(), e.getPrestigio(), e.getRutaEscudo()); }
}
