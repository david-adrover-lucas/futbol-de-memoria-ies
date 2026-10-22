package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.models.Carrera;
import com.auditoriria_ies.futbol_memoria.models.CarreraTemporada;
import com.auditoriria_ies.futbol_memoria.models.ClasificacionTemporada;
import com.auditoriria_ies.futbol_memoria.repository.CarreraTemporadaRepository;
import com.auditoriria_ies.futbol_memoria.repository.ClasificacionTemporadaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ClasificacionServicesTest {
    @Mock private ClasificacionTemporadaRepository repository;
    @Mock private CarreraTemporadaRepository temporadaRepository;

    @Test
    void devuelveLaClasificacionExistenteSinVolverASortearNiGuardar() {
        Carrera carrera = new Carrera();
        carrera.setCarreraId(12L);
        CarreraTemporada temporada = new CarreraTemporada();
        temporada.setCarrera(carrera);
        temporada.setNumeroTemporada(2);
        ClasificacionTemporada guardada = new ClasificacionTemporada();
        when(temporadaRepository.buscarParaCerrar(12L, 2)).thenReturn(Optional.of(temporada));
        when(repository.buscarParaActualizar(12L, 2)).thenReturn(Optional.of(guardada));

        ClasificacionServices service = new ClasificacionServices(repository, temporadaRepository, new ClasificadorTemporada(),
                new ClasificacionProperties(3, 6, 12, 45, 65, 80, 80));

        assertSame(guardada, service.clasificarAlCerrar(temporada, 18));
        verify(repository, never()).save(any(ClasificacionTemporada.class));
    }
}
