package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.ClasificacionTemporada;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClasificacionTemporadaRepository extends JpaRepository<ClasificacionTemporada, Long> {
    Optional<ClasificacionTemporada> findByCarreraCarreraIdAndNumeroTemporada(Long carreraId, Integer numeroTemporada);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select clasificacion from ClasificacionTemporada clasificacion where clasificacion.carrera.carreraId = :carreraId and clasificacion.numeroTemporada = :numeroTemporada")
    Optional<ClasificacionTemporada> buscarParaActualizar(@Param("carreraId") Long carreraId,
                                                          @Param("numeroTemporada") Integer numeroTemporada);
}
