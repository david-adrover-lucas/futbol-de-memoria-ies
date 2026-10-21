package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CarreraRepository extends JpaRepository<Carrera, Long> {
    Optional<Carrera> findByUsuarioUsuarioIdAndFechaEliminacionIsNull(Long usuarioId);
    Optional<Carrera> findByCarreraIdAndFechaEliminacionIsNull(Long carreraId);
    List<Carrera> findByFechaEliminacionIsNullOrderByExperienciaDescCarreraIdAsc();
    Page<Carrera> findByFechaEliminacionIsNull(Pageable pageable);
    List<Carrera> findByFechaEliminacionIsNullAndUsuarioFechaEliminacionIsNullOrderByExperienciaDescCarreraIdAsc();
    Page<Carrera> findByFechaEliminacionIsNullAndUsuarioFechaEliminacionIsNull(Pageable pageable);
}
