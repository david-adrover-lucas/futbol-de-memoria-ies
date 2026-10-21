package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.Competicion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CompeticionRepository extends JpaRepository<Competicion, Long> {
    Optional<Competicion> findByCodigo(String codigo);
    List<Competicion> findByTipoCompeticion(String tipoCompeticion);
}
