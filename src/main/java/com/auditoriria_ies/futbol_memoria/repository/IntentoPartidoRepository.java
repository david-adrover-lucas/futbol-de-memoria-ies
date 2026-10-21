package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.IntentoPartido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IntentoPartidoRepository extends JpaRepository<IntentoPartido, Long> {
    List<IntentoPartido> findByPartidoPartidoIdOrderByNumeroIntento(Long partidoId);
}
