package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.models.Usuario;
import com.auditoriria_ies.futbol_memoria.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsuarioServices {
    private final UsuarioRepository usuarioRepository;

    public UsuarioServices(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obtenerPerfilDeUsuario(Long usuarioId) {
        return buscarCuentaActiva(usuarioId);
    }

    @Transactional
    public Usuario actualizarDatosDePerfil(Long usuarioId, String nombreUsuario, String correoElectronico) {
        Usuario usuario = buscarCuentaActiva(usuarioId);
        validarCambiosDePerfil(nombreUsuario, correoElectronico, usuarioId);
        usuario.setNombreUsuario(nombreUsuario.trim());
        usuario.setCorreoElectronico(normalizarCorreo(correoElectronico));
        return usuarioRepository.save(usuario);
    }

    void verificarQueLaCuentaEsteActiva(Usuario usuario) {
        if (usuario == null || usuario.getFechaEliminacion() != null) {
            throw new RecursoNoEncontradoException("La cuenta no está disponible");
        }
    }

    Usuario buscarCuentaActiva(Long usuarioId) {
        if (usuarioId == null) throw new ReglaNegocioException("El ID del usuario es obligatorio");
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cuenta"));
        verificarQueLaCuentaEsteActiva(usuario);
        return usuario;
    }

    private void validarCambiosDePerfil(String nombreUsuario, String correoElectronico, Long usuarioId) {
        if (nombreUsuario == null || nombreUsuario.trim().length() < 3 || nombreUsuario.trim().length() > 40) {
            throw new ReglaNegocioException("El nombre de usuario debe tener entre 3 y 40 caracteres");
        }
        if (correoElectronico != null && !correoElectronico.isBlank()
                && (correoElectronico.length() > 254 || !correoElectronico.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
            throw new ReglaNegocioException("El correo electrónico no tiene un formato válido");
        }
        if (usuarioRepository.existsByNombreUsuarioAndUsuarioIdNot(nombreUsuario.trim(), usuarioId)) {
            throw new ReglaNegocioException("El nombre de usuario ya está en uso");
        }
        String correo = normalizarCorreo(correoElectronico);
        if (correo != null && usuarioRepository.existsByCorreoElectronicoAndUsuarioIdNot(correo, usuarioId)) {
            throw new ReglaNegocioException("El correo electrónico ya está en uso");
        }
    }

    private String normalizarCorreo(String correo) {
        return correo == null || correo.isBlank() ? null : correo.trim().toLowerCase();
    }
}
