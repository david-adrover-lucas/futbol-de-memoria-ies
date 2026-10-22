package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.CarreraTemporada;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CarreraTemporadaRepository extends JpaRepository<CarreraTemporada, Long> {
    Optional<CarreraTemporada> findByCarreraCarreraIdAndNumeroTemporada(Long carreraId, Integer numeroTemporada);
}
