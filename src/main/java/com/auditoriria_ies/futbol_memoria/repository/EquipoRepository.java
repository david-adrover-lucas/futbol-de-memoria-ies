package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    Optional<Equipo> findByCodigoExterno(String codigoExterno);
    List<Equipo> findByPaisOrderByPrestigioDesc(String pais);
    List<Equipo> findByEsInternacional(Boolean esInternacional);
}
