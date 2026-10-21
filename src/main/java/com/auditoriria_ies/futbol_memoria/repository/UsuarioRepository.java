package com.auditoriria_ies.futbol_memoria.repository;

import com.auditoriria_ies.futbol_memoria.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombreUsuarioAndFechaEliminacionIsNull(String nombreUsuario);
    Optional<Usuario> findByCorreoElectronicoAndFechaEliminacionIsNull(String correoElectronico);
    boolean existsByNombreUsuario(String nombreUsuario);
    boolean existsByCorreoElectronico(String correoElectronico);
}
