package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.CarreraTemporada;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CarreraTemporadaRepository extends JpaRepository<CarreraTemporada, Long> {
    Optional<CarreraTemporada> findByCarreraCarreraIdAndNumeroTemporada(Long carreraId, Integer numeroTemporada);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select temporada from CarreraTemporada temporada where temporada.carrera.carreraId = :carreraId and temporada.numeroTemporada = :numeroTemporada")
    Optional<CarreraTemporada> buscarParaCerrar(@Param("carreraId") Long carreraId,
                                                @Param("numeroTemporada") Integer numeroTemporada);
}
