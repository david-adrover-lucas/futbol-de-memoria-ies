package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.CarreraTorneo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CarreraTorneoRepository extends JpaRepository<CarreraTorneo, Long> {
    Optional<CarreraTorneo> findByCarreraCarreraIdAndNumeroTemporadaAndCodigoCompeticion(Long carreraId, Integer temporada, String codigo);
    Optional<CarreraTorneo> findByCarreraTorneoIdAndCarreraCarreraId(Long torneoId, Long carreraId);
}
