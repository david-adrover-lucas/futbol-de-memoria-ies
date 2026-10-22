package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.models.*;
import com.auditoriria_ies.futbol_memoria.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional(readOnly = true)
public class CarreraTorneoServices {
    private final CarreraServices carreraServices;
    private final CarreraTorneoRepository torneoRepository;
    private final TorneoFixtureRepository fixtureRepository;
    private final EquipoRepository equipoRepository;
    private final CompeticionServices competicionServices;
    private final ClasificacionServices clasificacionServices;

    public CarreraTorneoServices(CarreraServices carreraServices, CarreraTorneoRepository torneoRepository,
                                 TorneoFixtureRepository fixtureRepository, EquipoRepository equipoRepository,
                                 CompeticionServices competicionServices, ClasificacionServices clasificacionServices) {
        this.carreraServices = carreraServices; this.torneoRepository = torneoRepository;
        this.fixtureRepository = fixtureRepository; this.equipoRepository = equipoRepository;
        this.competicionServices = competicionServices;
        this.clasificacionServices = clasificacionServices;
    }

    public record EquipoVista(Long equipoId, String codigoExterno, String nombre, String apodo, Integer prestigio, String rutaEscudo) {}
    public record FixtureVista(Long fixtureId, Integer ronda, Integer numeroPartido, EquipoVista equipoLocal,
                               EquipoVista equipoVisitante, Integer golesLocal, Integer golesVisitante,
                               String estado, Long partidoId, boolean esPartidoDelUsuario) {}
    public record TorneoVista(Long torneoId, String codigoCompeticion, Integer numeroTemporada,
                              Integer rondaActual, String estado, List<FixtureVista> fixtures) {}

    @Transactional
    public TorneoVista iniciarObtener(Long usuarioId, String codigo) {
        Carrera carrera = carreraServices.obtenerCarreraActivaPorUsuario(usuarioId);
        Competicion comp = competicionServices.obtenerCompeticionPorCodigo(codigo);
        if (!"copa".equals(comp.getTipoCompeticion())) throw new ReglaNegocioException("La competición seleccionada no es una copa");
        validarClasificacionInternacional(carrera, codigo);
        CarreraTorneo torneo = torneoRepository.findByCarreraCarreraIdAndNumeroTemporadaAndCodigoCompeticion(
                carrera.getCarreraId(), carrera.getNumeroTemporada(), codigo).orElseGet(() -> crearTorneo(carrera, codigo));
        List<TorneoFixture> ronda = fixtureRepository.findByTorneoCarreraTorneoIdAndRondaOrderByNumeroPartido(torneo.getCarreraTorneoId(), torneo.getRondaActual());
        if (ronda.isEmpty() && "en_curso".equals(torneo.getEstado())) ronda = crearRonda(torneo, participantesIniciales(carrera), 1);
        return vista(torneo, carrera);
    }

    public TorneoVista obtener(Long usuarioId, String codigo) {
        Carrera carrera = carreraServices.obtenerCarreraActivaPorUsuario(usuarioId);
        CarreraTorneo torneo = torneoRepository.findByCarreraCarreraIdAndNumeroTemporadaAndCodigoCompeticion(
                carrera.getCarreraId(), carrera.getNumeroTemporada(), codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Todavía no participas en esta copa"));
        return vista(torneo, carrera);
    }

    @Transactional
    public FixtureVista siguientePartido(Long usuarioId, String codigo) {
        TorneoVista ignored = iniciarObtener(usuarioId, codigo);
        Carrera carrera = carreraServices.obtenerCarreraActivaPorUsuario(usuarioId);
        CarreraTorneo torneo = torneoRepository.findByCarreraCarreraIdAndNumeroTemporadaAndCodigoCompeticion(
                carrera.getCarreraId(), carrera.getNumeroTemporada(), codigo).orElseThrow();
        if (!"en_curso".equals(torneo.getEstado())) throw new ReglaNegocioException("La participación en esta copa ya terminó: " + torneo.getEstado());
        TorneoFixture fixture = fixtureRepository.findByTorneoCarreraTorneoIdAndRondaOrderByNumeroPartido(torneo.getCarreraTorneoId(), torneo.getRondaActual())
                .stream().filter(f -> f.getEquipoLocal().getEquipoId().equals(carrera.getEquipoActual().getEquipoId())
                        || f.getEquipoVisitante().getEquipoId().equals(carrera.getEquipoActual().getEquipoId()))
                .findFirst().orElseThrow(() -> new RecursoNoEncontradoException("No se encontró tu partido de copa"));
        if (!"pendiente".equals(fixture.getEstado())) throw new ReglaNegocioException("Ya jugaste esta ronda de la copa");
        return fixtureVista(fixture, carrera.getEquipoActual().getEquipoId());
    }

    @Transactional
    public TorneoFixture validarYObtenerFixture(Long fixtureId, Long usuarioId, String codigo) {
        Carrera carrera = carreraServices.obtenerCarreraActivaPorUsuario(usuarioId);
        validarClasificacionInternacional(carrera, codigo);
        TorneoFixture fixture = fixtureRepository.findByTorneoFixtureIdAndTorneoCarreraCarreraId(fixtureId, carrera.getCarreraId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ese partido de copa"));
        if (!codigo.equals(fixture.getTorneo().getCodigoCompeticion())) throw new ReglaNegocioException("El fixture pertenece a otra copa");
        if (!"pendiente".equals(fixture.getEstado()) || fixture.getPartido() != null) throw new ReglaNegocioException("El partido de copa ya fue iniciado o finalizado");
        if (!fixture.getEquipoLocal().getEquipoId().equals(carrera.getEquipoActual().getEquipoId())
                || !fixture.getTorneo().getEquipoParticipante().getEquipoId().equals(carrera.getEquipoActual().getEquipoId())) {
            throw new ReglaNegocioException("El fixture no corresponde al equipo actual de la carrera");
        }
        return fixture;
    }

    private void validarClasificacionInternacional(Carrera carrera, String codigo) {
        if (!"libertadores".equals(codigo) && !"sudamericana".equals(codigo)) return;
        if (!clasificacionServices.estaClasificadoParaTemporada(carrera.getCarreraId(),
                carrera.getNumeroTemporada(), carrera.getEquipoActual().getEquipoId(), codigo)) {
            throw new ReglaNegocioException("Tu equipo no está clasificado a este torneo internacional en esta temporada");
        }
    }

    @Transactional
    public void asignarFixture(TorneoFixture fixture, Partido partido) { fixture.setPartido(partido); fixtureRepository.save(fixture); }

    @Transactional
    public void registrarResultado(Partido partido) {
        fixtureRepository.findByPartidoPartidoId(partido.getPartidoId()).ifPresent(fixture -> {
            fixture.setGolesLocal(partido.getGolesLocal()); fixture.setGolesVisitante(partido.getGolesVisitante());
            if (Objects.equals(partido.getGolesLocal(), partido.getGolesVisitante())) {
                fixture.setEstado("pendiente_penales");
                fixtureRepository.save(fixture);
                return;
            }
            fixture.setEstado("finalizado"); fixtureRepository.save(fixture);
            resolverGanadorFixture(fixture, partido.getGolesLocal() > partido.getGolesVisitante()
                    ? fixture.getEquipoLocal() : fixture.getEquipoVisitante());
        });
    }

    @Transactional
    public void registrarGanadorPorPenales(Long partidoId, Long equipoGanadorId) {
        TorneoFixture fixture = fixtureRepository.findByPartidoPartidoId(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El partido no pertenece a una eliminatoria"));
        if (!"pendiente_penales".equals(fixture.getEstado())) {
            if ("finalizado".equals(fixture.getEstado())) return;
            throw new ReglaNegocioException("La eliminatoria no está esperando una tanda de penales");
        }
        Equipo ganador = fixture.getEquipoLocal().getEquipoId().equals(equipoGanadorId)
                ? fixture.getEquipoLocal() : fixture.getEquipoVisitante().getEquipoId().equals(equipoGanadorId)
                ? fixture.getEquipoVisitante() : null;
        if (ganador == null) throw new ReglaNegocioException("El ganador de los penales no pertenece a este partido");
        fixture.setEstado("finalizado");
        fixtureRepository.save(fixture);
        resolverGanadorFixture(fixture, ganador);
    }

    private void resolverGanadorFixture(TorneoFixture fixture, Equipo ganadorFixture) {
            CarreraTorneo torneo = fixture.getTorneo();
            boolean ganaUsuario = ganadorFixture.getEquipoId().equals(torneo.getEquipoParticipante().getEquipoId());
            if (!ganaUsuario) { torneo.setEstado("eliminado"); torneoRepository.save(torneo); return; }
            if (torneo.getRondaActual() >= 4) { torneo.setEstado("campeon"); torneoRepository.save(torneo); return; }
            List<TorneoFixture> ronda = fixtureRepository.findByTorneoCarreraTorneoIdAndRondaOrderByNumeroPartido(torneo.getCarreraTorneoId(), torneo.getRondaActual());
            List<Equipo> ganadores = ronda.stream().map(f -> {
                if (f.getEquipoLocal().getEquipoId().equals(ganadorFixture.getEquipoId())) return ganadorFixture;
                if (f.getEquipoVisitante().getEquipoId().equals(ganadorFixture.getEquipoId())) return ganadorFixture;
                return f.getGolesLocal() > f.getGolesVisitante() ? f.getEquipoLocal() : f.getEquipoVisitante();
            }).toList();
            torneo.setRondaActual(torneo.getRondaActual() + 1); torneoRepository.save(torneo);
            crearRonda(torneo, ganadores, torneo.getRondaActual(), torneo.getEquipoParticipante());
    }

    private CarreraTorneo crearTorneo(Carrera carrera, String codigo) {
        CarreraTorneo torneo = new CarreraTorneo(); torneo.setCarrera(carrera); torneo.setCodigoCompeticion(codigo);
        torneo.setEquipoParticipante(carrera.getEquipoActual());
        torneo.setNumeroTemporada(carrera.getNumeroTemporada()); torneo.setRondaActual(1); torneo.setEstado("en_curso");
        return torneoRepository.save(torneo);
    }

    private List<Equipo> participantesIniciales(Carrera carrera) {
        List<Equipo> equipos = new ArrayList<>(equipoRepository.findAll());
        equipos.removeIf(e -> e.getEquipoId().equals(carrera.getEquipoActual().getEquipoId()));
        Collections.shuffle(equipos);
        if (equipos.size() < 15) throw new ReglaNegocioException("La base necesita al menos 16 equipos para crear una copa");
        List<Equipo> participantes = new ArrayList<>(); participantes.add(carrera.getEquipoActual()); participantes.addAll(equipos.subList(0, 15));
        Collections.shuffle(participantes.subList(1, participantes.size()));
        return participantes;
    }

    private List<TorneoFixture> crearRonda(CarreraTorneo torneo, List<Equipo> participantes, int ronda) {
        return crearRonda(torneo, participantes, ronda, torneo.getEquipoParticipante());
    }

    private List<TorneoFixture> crearRonda(CarreraTorneo torneo, List<Equipo> participantes, int ronda, Equipo equipoUsuario) {
        if (participantes.size() < 2 || participantes.size() % 2 != 0) throw new ReglaNegocioException("No se pudo completar la llave de la copa");
        Carrera carrera = torneo.getCarrera();
        Long userTeam = equipoUsuario.getEquipoId();
        List<Equipo> lista = new ArrayList<>(participantes);
        int userIndex = -1;
        for (int i = 0; i < lista.size(); i++) if (lista.get(i).getEquipoId().equals(userTeam)) userIndex = i;
        if (userIndex < 0) throw new ReglaNegocioException("Tu equipo ya no está en la llave de la copa");
        Collections.swap(lista, 0, userIndex);
        if (lista.size() > 2) Collections.shuffle(lista.subList(2, lista.size()));
        List<TorneoFixture> fixtures = new ArrayList<>();
        for (int i = 0; i < lista.size(); i += 2) {
            TorneoFixture fixture = new TorneoFixture(); fixture.setTorneo(torneo); fixture.setRonda(ronda);
            fixture.setNumeroPartido(i / 2 + 1); fixture.setEquipoLocal(lista.get(i)); fixture.setEquipoVisitante(lista.get(i + 1));
            if (i != 0) { fixture.setGolesLocal(ThreadLocalRandom.current().nextInt(5)); fixture.setGolesVisitante(ThreadLocalRandom.current().nextInt(5));
                if (Objects.equals(fixture.getGolesLocal(), fixture.getGolesVisitante())) fixture.setGolesLocal(fixture.getGolesLocal() + 1);
                fixture.setEstado("finalizado"); }
            fixtures.add(fixture);
        }
        return fixtureRepository.saveAll(fixtures);
    }

    private TorneoVista vista(CarreraTorneo torneo, Carrera carrera) {
        return new TorneoVista(torneo.getCarreraTorneoId(), torneo.getCodigoCompeticion(), torneo.getNumeroTemporada(),
                torneo.getRondaActual(), torneo.getEstado(), fixtureRepository.findByTorneoCarreraTorneoIdOrderByRondaAscNumeroPartidoAsc(torneo.getCarreraTorneoId())
                .stream().map(f -> fixtureVista(f, torneo.getEquipoParticipante().getEquipoId())).toList());
    }

    private FixtureVista fixtureVista(TorneoFixture f, Long equipoUsuario) {
        return new FixtureVista(f.getTorneoFixtureId(), f.getRonda(), f.getNumeroPartido(), equipoVista(f.getEquipoLocal()),
                equipoVista(f.getEquipoVisitante()), f.getGolesLocal(), f.getGolesVisitante(), f.getEstado(),
                f.getPartido() == null ? null : f.getPartido().getPartidoId(),
                f.getEquipoLocal().getEquipoId().equals(equipoUsuario) || f.getEquipoVisitante().getEquipoId().equals(equipoUsuario));
    }
    private EquipoVista equipoVista(Equipo e) { return new EquipoVista(e.getEquipoId(), e.getCodigoExterno(), e.getNombre(), e.getApodo(), e.getPrestigio(), e.getRutaEscudo()); }
}
