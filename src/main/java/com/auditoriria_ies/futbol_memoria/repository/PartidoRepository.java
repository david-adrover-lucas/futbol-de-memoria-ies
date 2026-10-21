package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;

public interface PartidoRepository extends JpaRepository<Partido, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Partido p where p.partidoId = :partidoId")
    java.util.Optional<Partido> buscarParaActualizar(@Param("partidoId") Long partidoId);
    List<Partido> findByCarreraCarreraIdOrderByIniciadoEnDesc(Long carreraId);
    List<Partido> findByEstadoOrderByIniciadoEnDesc(String estado);
}
