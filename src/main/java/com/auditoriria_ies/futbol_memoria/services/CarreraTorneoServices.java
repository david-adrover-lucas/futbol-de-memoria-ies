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
    private final CalendarioFechaRepository calendarioRepository;
    private final CarreraTemporadaRepository temporadaRepository;

    public CarreraTorneoServices(CarreraServices carreraServices, CarreraTorneoRepository torneoRepository,
                                 TorneoFixtureRepository fixtureRepository, EquipoRepository equipoRepository,
                                 CompeticionServices competicionServices, ClasificacionServices clasificacionServices,
                                 CalendarioFechaRepository calendarioRepository, CarreraTemporadaRepository temporadaRepository) {
        this.carreraServices = carreraServices; this.torneoRepository = torneoRepository;
        this.fixtureRepository = fixtureRepository; this.equipoRepository = equipoRepository;
        this.competicionServices = competicionServices;
        this.clasificacionServices = clasificacionServices;
        this.calendarioRepository = calendarioRepository;
        this.temporadaRepository = temporadaRepository;
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
        validarEventoCalendarioActual(carrera, codigo);
        CarreraTorneo torneo = torneoRepository.findByCarreraCarreraIdAndNumeroTemporadaAndCodigoCompeticion(
                carrera.getCarreraId(), carrera.getNumeroTemporada(), codigo).orElseGet(() -> crearTorneo(carrera, codigo));
        List<TorneoFixture> ronda = fixtureRepository.findByTorneoCarreraTorneoIdAndRondaOrderByNumeroPartido(torneo.getCarreraTorneoId(), torneo.getRondaActual());
        if (ronda.isEmpty() && "en_curso".equals(torneo.getEstado())) {
            ronda = crearRonda(torneo, participantesIniciales(carrera, codigo, true), 1);
        }
        simularPartidosCPU(ronda, torneo.getEquipoParticipante());
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
        validarEventoCalendarioActual(carrera, codigo);
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
            completarFechaTorneo(fixture);
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
        completarFechaTorneo(fixture);
    }

    @Transactional
    public void simularRonda(Long usuarioId, Integer numeroTemporada, String codigo, Integer rondaEsperada) {
        Carrera carrera = carreraServices.obtenerCarreraActivaPorUsuario(usuarioId);
        Long carreraId = carrera.getCarreraId();
        CarreraTorneo torneo = torneoRepository.findByCarreraCarreraIdAndNumeroTemporadaAndCodigoCompeticion(
                carreraId, numeroTemporada, codigo).orElseGet(() -> crearTorneoSimulado(carrera, codigo));
        if (torneo.getRondaActual() != rondaEsperada) {
            if (torneo.getRondaActual() > rondaEsperada || "campeon".equals(torneo.getEstado())) return;
            throw new ReglaNegocioException("La ronda del torneo no coincide con el calendario");
        }
        if (torneo.getEquipoParticipante() != null && "en_curso".equals(torneo.getEstado()))
            throw new ReglaNegocioException("Tu equipo debe jugar su partido antes de simular esta ronda");
        List<TorneoFixture> ronda = fixtureRepository.findByTorneoCarreraTorneoIdAndRondaOrderByNumeroPartido(
                torneo.getCarreraTorneoId(), rondaEsperada);
        simularPartidosCPU(ronda, null);
        if (rondaEsperada >= 4) {
            if (torneo.getEquipoParticipante() == null) torneo.setEstado("campeon");
            torneoRepository.save(torneo);
            return;
        }
        torneo.setRondaActual(rondaEsperada + 1);
        torneoRepository.save(torneo);
        crearRonda(torneo, ganadores(ronda), torneo.getRondaActual(), null);
    }

    private void resolverGanadorFixture(TorneoFixture fixture, Equipo ganadorFixture) {
        CarreraTorneo torneo = fixture.getTorneo();
        Equipo participante = torneo.getEquipoParticipante();
        if (participante == null) throw new ReglaNegocioException("El fixture no tiene equipo participante del usuario");
        boolean ganaUsuario = ganadorFixture.getEquipoId().equals(participante.getEquipoId());
        if (torneo.getRondaActual() >= 4) {
            torneo.setEstado(ganaUsuario ? "campeon" : "eliminado");
            torneoRepository.save(torneo);
            return;
        }
        List<TorneoFixture> ronda = fixtureRepository.findByTorneoCarreraTorneoIdAndRondaOrderByNumeroPartido(
                torneo.getCarreraTorneoId(), torneo.getRondaActual());
        List<Equipo> ganadores = ganadores(ronda);
        torneo.setRondaActual(torneo.getRondaActual() + 1);
        torneo.setEstado(ganaUsuario ? "en_curso" : "eliminado");
        torneoRepository.save(torneo);
        crearRonda(torneo, ganadores, torneo.getRondaActual(), ganaUsuario ? participante : null);
    }

    private List<Equipo> ganadores(List<TorneoFixture> ronda) {
        return ronda.stream().map(f -> f.getGolesLocal() > f.getGolesVisitante()
                ? f.getEquipoLocal() : f.getEquipoVisitante()).toList();
    }

    private CarreraTorneo crearTorneo(Carrera carrera, String codigo) {
        return crearTorneo(carrera, codigo, carrera.getEquipoActual());
    }

    private CarreraTorneo crearTorneo(Carrera carrera, String codigo, Equipo participante) {
        CarreraTorneo torneo = new CarreraTorneo();
        torneo.setCarrera(carrera); torneo.setCodigoCompeticion(codigo); torneo.setEquipoParticipante(participante);
        torneo.setNumeroTemporada(carrera.getNumeroTemporada()); torneo.setRondaActual(1); torneo.setEstado("en_curso");
        return torneoRepository.save(torneo);
    }

    private CarreraTorneo crearTorneoSimulado(Carrera carrera, String codigo) {
        CarreraTorneo torneo = crearTorneo(carrera, codigo, null);
        crearRonda(torneo, participantesIniciales(carrera, codigo, false), 1, null);
        return torneo;
    }

    private List<Equipo> participantesIniciales(Carrera carrera, String codigo, boolean incluirUsuario) {
        List<Equipo> candidatos = new ArrayList<>(equipoRepository.findAll());
        if ("libertadores".equals(codigo) || "sudamericana".equals(codigo)) {
            candidatos.removeIf(e -> !Boolean.TRUE.equals(e.getEsInternacional()));
        } else {
            candidatos.removeIf(e -> !"Argentina".equalsIgnoreCase(e.getPais()));
        }
        Equipo usuario = carrera.getEquipoActual();
        if (incluirUsuario) candidatos.removeIf(e -> e.getEquipoId().equals(usuario.getEquipoId()));
        Collections.shuffle(candidatos);
        int necesarios = incluirUsuario ? 15 : 16;
        if (candidatos.size() < necesarios) throw new ReglaNegocioException("La base no tiene suficientes equipos para completar la copa");
        List<Equipo> participantes = new ArrayList<>();
        if (incluirUsuario) participantes.add(usuario);
        participantes.addAll(candidatos.subList(0, necesarios));
        if (incluirUsuario && participantes.size() > 2) Collections.shuffle(participantes.subList(1, participantes.size()));
        return participantes;
    }

    private List<TorneoFixture> crearRonda(CarreraTorneo torneo, List<Equipo> participantes, int ronda) {
        return crearRonda(torneo, participantes, ronda, torneo.getEquipoParticipante());
    }

    private List<TorneoFixture> crearRonda(CarreraTorneo torneo, List<Equipo> participantes, int ronda, Equipo equipoUsuario) {
        if (participantes.size() < 2 || participantes.size() % 2 != 0) throw new ReglaNegocioException("No se pudo completar la llave de la copa");
        List<Equipo> lista = new ArrayList<>(participantes);
        if (equipoUsuario != null) {
            int indice = -1;
            for (int i = 0; i < lista.size(); i++) if (lista.get(i).getEquipoId().equals(equipoUsuario.getEquipoId())) indice = i;
            if (indice < 0) throw new ReglaNegocioException("Tu equipo ya no está en la llave de la copa");
            Collections.swap(lista, 0, indice);
            if (lista.size() > 2) Collections.shuffle(lista.subList(2, lista.size()));
        } else Collections.shuffle(lista);
        List<TorneoFixture> fixtures = new ArrayList<>();
        for (int i = 0; i < lista.size(); i += 2) {
            TorneoFixture fixture = new TorneoFixture(); fixture.setTorneo(torneo); fixture.setRonda(ronda);
            fixture.setNumeroPartido(i / 2 + 1); fixture.setEquipoLocal(lista.get(i)); fixture.setEquipoVisitante(lista.get(i + 1));
            fixtures.add(fixture);
        }
        return fixtureRepository.saveAll(fixtures);
    }

    private void simularPartidosCPU(List<TorneoFixture> fixtures, Equipo equipoUsuario) {
        for (TorneoFixture fixture : fixtures) {
            if (!"pendiente".equals(fixture.getEstado())) continue;
            boolean perteneceAlUsuario = equipoUsuario != null && (fixture.getEquipoLocal().getEquipoId().equals(equipoUsuario.getEquipoId())
                    || fixture.getEquipoVisitante().getEquipoId().equals(equipoUsuario.getEquipoId()));
            if (perteneceAlUsuario) continue;
            boolean localGana = ThreadLocalRandom.current().nextDouble() < probabilidadVictoriaLocal(
                    fixture.getEquipoLocal(), fixture.getEquipoVisitante());
            int golesGanador = ThreadLocalRandom.current().nextInt(1, 5);
            int golesPerdedor = ThreadLocalRandom.current().nextInt(golesGanador);
            fixture.setGolesLocal(localGana ? golesGanador : golesPerdedor);
            fixture.setGolesVisitante(localGana ? golesPerdedor : golesGanador);
            fixture.setEstado("finalizado");
        }
        fixtureRepository.saveAll(fixtures);
    }

    private double probabilidadVictoriaLocal(Equipo local, Equipo visitante) {
        return Math.max(0.25, Math.min(0.75, 0.50 + (local.getPrestigio() - visitante.getPrestigio()) * 0.075));
    }

    private void validarEventoCalendarioActual(Carrera carrera, String codigo) {
        CarreraTemporada temporada = temporadaRepository.findByCarreraCarreraIdAndNumeroTemporada(
                        carrera.getCarreraId(), carrera.getNumeroTemporada())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la temporada actual"));
        CalendarioFecha evento = calendarioRepository.findFirstByTemporadaCarreraTemporadaIdAndEstadoOrderByNumeroFechaAsc(
                        temporada.getCarreraTemporadaId(), "pendiente")
                .orElseThrow(() -> new ReglaNegocioException("La temporada no tiene una fecha pendiente"));
        if (!codigo.equals(evento.getCompeticion().getCodigo())) throw new ReglaNegocioException("Esta competición no corresponde a la fecha actual del calendario");
    }

    private void completarFechaTorneo(TorneoFixture fixture) {
        CarreraTemporada temporada = temporadaRepository.findByCarreraCarreraIdAndNumeroTemporada(
                        fixture.getTorneo().getCarrera().getCarreraId(), fixture.getTorneo().getNumeroTemporada())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la temporada del torneo"));
        calendarioRepository.findByTemporadaCarreraTemporadaIdAndCompeticionCodigoAndNumeroRonda(
                        temporada.getCarreraTemporadaId(), fixture.getTorneo().getCodigoCompeticion(), fixture.getRonda())
                .ifPresent(fecha -> { fecha.setEstado("completada"); fecha.setCompletadaEn(java.time.LocalDateTime.now()); calendarioRepository.save(fecha); });
    }
    private TorneoVista vista(CarreraTorneo torneo, Carrera carrera) {
        return new TorneoVista(torneo.getCarreraTorneoId(), torneo.getCodigoCompeticion(), torneo.getNumeroTemporada(),
                torneo.getRondaActual(), torneo.getEstado(), fixtureRepository.findByTorneoCarreraTorneoIdOrderByRondaAscNumeroPartidoAsc(torneo.getCarreraTorneoId())
                .stream().map(f -> fixtureVista(f, torneo.getEquipoParticipante() == null ? null : torneo.getEquipoParticipante().getEquipoId())).toList());
    }

    private FixtureVista fixtureVista(TorneoFixture f, Long equipoUsuario) {
        return new FixtureVista(f.getTorneoFixtureId(), f.getRonda(), f.getNumeroPartido(), equipoVista(f.getEquipoLocal()),
                equipoVista(f.getEquipoVisitante()), f.getGolesLocal(), f.getGolesVisitante(), f.getEstado(),
                f.getPartido() == null ? null : f.getPartido().getPartidoId(),
                equipoUsuario != null && (f.getEquipoLocal().getEquipoId().equals(equipoUsuario) || f.getEquipoVisitante().getEquipoId().equals(equipoUsuario)));
    }
    private EquipoVista equipoVista(Equipo e) { return new EquipoVista(e.getEquipoId(), e.getCodigoExterno(), e.getNombre(), e.getApodo(), e.getPrestigio(), e.getRutaEscudo()); }
}
