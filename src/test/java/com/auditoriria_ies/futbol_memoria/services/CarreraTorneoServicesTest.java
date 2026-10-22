package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.models.Carrera;
import com.auditoriria_ies.futbol_memoria.models.CarreraTorneo;
import com.auditoriria_ies.futbol_memoria.models.Equipo;
import com.auditoriria_ies.futbol_memoria.models.TorneoFixture;
import com.auditoriria_ies.futbol_memoria.repository.CalendarioFechaRepository;
import com.auditoriria_ies.futbol_memoria.repository.CarreraTemporadaRepository;
import com.auditoriria_ies.futbol_memoria.repository.CarreraTorneoRepository;
import com.auditoriria_ies.futbol_memoria.repository.EquipoRepository;
import com.auditoriria_ies.futbol_memoria.repository.TandaPenalesRepository;
import com.auditoriria_ies.futbol_memoria.repository.TorneoFixtureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarreraTorneoServicesTest {
    @Mock private CarreraServices carreraServices;
    @Mock private CarreraTorneoRepository torneoRepository;
    @Mock private TorneoFixtureRepository fixtureRepository;
    @Mock private EquipoRepository equipoRepository;
    @Mock private CompeticionServices competicionServices;
    @Mock private ClasificacionServices clasificacionServices;
    @Mock private CalendarioFechaRepository calendarioRepository;
    @Mock private CarreraTemporadaRepository temporadaRepository;
    @Mock private TandaPenalesRepository tandaPenalesRepository;

    @Test
    void simularRondaPersisteResultadosYAvanzaSoloConLosGanadores() {
        Carrera carrera = new Carrera();
        carrera.setCarreraId(25L);
        carrera.setNumeroTemporada(2);

        CarreraTorneo torneo = new CarreraTorneo();
        torneo.setCarreraTorneoId(90L);
        torneo.setCarrera(carrera);
        torneo.setCodigoCompeticion("libertadores");
        torneo.setNumeroTemporada(2);
        torneo.setRondaActual(1);
        torneo.setEstado("eliminado");

        List<TorneoFixture> primeraRonda = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            TorneoFixture fixture = new TorneoFixture();
            fixture.setTorneoFixtureId((long) i + 1);
            fixture.setTorneo(torneo);
            fixture.setRonda(1);
            fixture.setNumeroPartido(i + 1);
            fixture.setEquipoLocal(equipo(i * 2 + 1));
            fixture.setEquipoVisitante(equipo(i * 2 + 2));
            primeraRonda.add(fixture);
        }

        when(carreraServices.obtenerCarreraActivaPorUsuario(7L)).thenReturn(carrera);
        when(torneoRepository.findByCarreraCarreraIdAndNumeroTemporadaAndCodigoCompeticion(
                25L, 2, "libertadores")).thenReturn(Optional.of(torneo));
        when(fixtureRepository.findByTorneoCarreraTorneoIdAndRondaOrderByNumeroPartido(90L, 1))
                .thenReturn(primeraRonda);

        CarreraTorneoServices service = new CarreraTorneoServices(carreraServices, torneoRepository,
                fixtureRepository, equipoRepository, competicionServices, clasificacionServices,
                calendarioRepository, temporadaRepository, tandaPenalesRepository);

        service.simularRonda(7L, 2, "libertadores", 1);

        ArgumentCaptor<Iterable<TorneoFixture>> guardados = ArgumentCaptor.forClass(Iterable.class);
        verify(fixtureRepository, times(2)).saveAll(guardados.capture());
        List<List<TorneoFixture>> gruposGuardados = guardados.getAllValues().stream()
                .map(fixtures -> StreamSupport.stream(fixtures.spliterator(), false).toList())
                .toList();

        List<TorneoFixture> resultados = gruposGuardados.get(0);
        assertEquals(8, resultados.size());
        List<Long> idsGanadores = new ArrayList<>();
        for (TorneoFixture resultado : resultados) {
            assertEquals("finalizado", resultado.getEstado());
            assertNotNull(resultado.getGolesLocal());
            assertNotNull(resultado.getGolesVisitante());
            assertFalse(resultado.getGolesLocal().equals(resultado.getGolesVisitante()));
            idsGanadores.add(resultado.getGolesLocal() > resultado.getGolesVisitante()
                    ? resultado.getEquipoLocal().getEquipoId() : resultado.getEquipoVisitante().getEquipoId());
        }

        List<TorneoFixture> segundaRonda = gruposGuardados.get(1);
        assertEquals(4, segundaRonda.size());
        assertEquals(2, torneo.getRondaActual());
        assertEquals(idsGanadores.stream().sorted().toList(), segundaRonda.stream()
                .flatMap(fixture -> List.of(fixture.getEquipoLocal().getEquipoId(),
                        fixture.getEquipoVisitante().getEquipoId()).stream())
                .sorted().toList());
        segundaRonda.forEach(fixture -> assertEquals(2, fixture.getRonda()));
    }

    private Equipo equipo(long id) {
        Equipo equipo = new Equipo();
        equipo.setEquipoId(id);
        equipo.setPrestigio((int) (id % 5) + 1);
        return equipo;
    }
}
