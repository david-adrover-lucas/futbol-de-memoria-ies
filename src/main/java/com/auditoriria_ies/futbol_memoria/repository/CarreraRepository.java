package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CarreraRepository extends JpaRepository<Carrera, Long> {
    Optional<Carrera> findByUsuarioUsuarioIdAndFechaEliminacionIsNull(Long usuarioId);
    List<Carrera> findByFechaEliminacionIsNullOrderByExperienciaDescCarreraIdAsc();
}
