package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.TandaPenales;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface TandaPenalesRepository extends JpaRepository<TandaPenales, Long> {
    Optional<TandaPenales> findByPartidoPartidoId(Long partidoId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TandaPenales t where t.partido.partidoId = :partidoId")
    Optional<TandaPenales> buscarParaActualizar(@Param("partidoId") Long partidoId);
}
