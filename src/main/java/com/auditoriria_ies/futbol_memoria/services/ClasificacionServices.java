package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.models.CarreraTemporada;
import com.auditoriria_ies.futbol_memoria.models.ClasificacionTemporada;
import com.auditoriria_ies.futbol_memoria.models.Equipo;
import com.auditoriria_ies.futbol_memoria.repository.CarreraTemporadaRepository;
import com.auditoriria_ies.futbol_memoria.repository.ClasificacionTemporadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional(readOnly = true)
public class ClasificacionServices {
    private static final int TIRADA_MINIMA = 1;
    private static final int TIRADA_MAXIMA_EXCLUSIVA = 101;
    private final ClasificacionTemporadaRepository clasificacionRepository;
    private final CarreraTemporadaRepository temporadaRepository;
    private final ClasificadorTemporada clasificador;
    private final ClasificacionProperties reglas;

    public ClasificacionServices(ClasificacionTemporadaRepository clasificacionRepository,
                                 CarreraTemporadaRepository temporadaRepository,
                                 ClasificadorTemporada clasificador, ClasificacionProperties reglas) {
        this.clasificacionRepository = clasificacionRepository;
        this.temporadaRepository = temporadaRepository;
        this.clasificador = clasificador;
        this.reglas = reglas;
    }

    @Transactional
    public ClasificacionTemporada clasificarAlCerrar(CarreraTemporada temporada, int posicionFinal) {
        Long carreraId = temporada.getCarrera().getCarreraId();
        Integer numeroTemporada = temporada.getNumeroTemporada();
        CarreraTemporada temporadaBloqueada = temporadaRepository.buscarParaCerrar(carreraId, numeroTemporada).orElse(temporada);
        return clasificacionRepository.buscarParaActualizar(carreraId, numeroTemporada)
                .orElseGet(() -> guardarClasificacion(temporadaBloqueada, posicionFinal));
    }

    public boolean estaClasificadoParaTemporada(Long carreraId, int numeroTemporada, Long equipoId,
                                                String codigoCompeticion) {
        if (numeroTemporada <= 1) return false;
        return clasificacionRepository.findByCarreraCarreraIdAndNumeroTemporada(carreraId, numeroTemporada - 1)
                .map(clasificacion -> Objects.equals(clasificacion.getCodigoCompeticion(), codigoCompeticion)
                        && Objects.equals(clasificacion.getEquipo().getEquipoId(), equipoId))
                .orElse(false);
    }

    private ClasificacionTemporada guardarClasificacion(CarreraTemporada temporada, int posicionFinal) {
        Equipo equipo = temporada.getCarrera().getEquipoActual();
        ClasificadorTemporada.Decision decision = clasificador.decidir(equipo.getPrestigio(), posicionFinal,
                reglas, () -> ThreadLocalRandom.current().nextInt(TIRADA_MINIMA, TIRADA_MAXIMA_EXCLUSIVA));

        ClasificacionTemporada clasificacion = new ClasificacionTemporada();
        clasificacion.setCarrera(temporada.getCarrera());
        clasificacion.setEquipo(equipo);
        clasificacion.setNumeroTemporada(temporada.getNumeroTemporada());
        clasificacion.setPrestigioEquipo(equipo.getPrestigio());
        clasificacion.setPosicionFinal(posicionFinal);
        clasificacion.setCodigoCompeticion(decision.codigoCompeticion());
        clasificacion.setMetodo(decision.metodo());
        clasificacion.setTiradaLibertadores(decision.tiradaLibertadores());
        clasificacion.setTiradaSudamericana(decision.tiradaSudamericana());
        clasificacion.setProbabilidadLibertadores(decision.tiradaLibertadores() == null ? null
                : reglas.probabilidadLibertadores(equipo.getPrestigio()));
        clasificacion.setProbabilidadSudamericana(decision.tiradaSudamericana() == null ? null
                : reglas.probabilidadSudamericana(equipo.getPrestigio()));
        return clasificacionRepository.save(clasificacion);
    }
}
