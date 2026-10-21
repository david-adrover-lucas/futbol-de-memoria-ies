package com.auditoriria_ies.futbol_memoria.services;

import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import com.auditoriria_ies.futbol_memoria.models.Usuario;
import com.auditoriria_ies.futbol_memoria.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;

@Service
@Transactional(readOnly = true)
public class AutenticacionServices {
    private static final String ERROR_AUTENTICACION = "Usuario o contraseña incorrectos";
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AutenticacionServices(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Long registrarUsuario(String nombreUsuario, String correoElectronico, String contrasena) {
        validarDatosParaRegistro(nombreUsuario, correoElectronico, contrasena);
        verificarDisponibilidadDeNombreYCorreo(nombreUsuario, correoElectronico);
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombreUsuario.trim());
        usuario.setCorreoElectronico(normalizarCorreo(correoElectronico));
        usuario.setHashContrasena(generarHashDeContrasena(contrasena));
        return usuarioRepository.save(usuario).getUsuarioId();
    }

    public Long iniciarSesion(String nombreUsuario, String contrasena) {
        if (nombreUsuario == null || contrasena == null) throw new ReglaNegocioException(ERROR_AUTENTICACION);
        Usuario usuario = usuarioRepository.findByNombreUsuarioAndFechaEliminacionIsNull(nombreUsuario.trim())
                .orElseThrow(() -> new ReglaNegocioException(ERROR_AUTENTICACION));
        if (!verificarContrasenaIngresada(contrasena, usuario.getHashContrasena())) {
            throw new ReglaNegocioException(ERROR_AUTENTICACION);
        }
        return usuario.getUsuarioId();
    }

    private void validarDatosParaRegistro(String nombreUsuario, String correo, String contrasena) {
        if (nombreUsuario == null || nombreUsuario.trim().length() < 3 || nombreUsuario.trim().length() > 40) {
            throw new ReglaNegocioException("El nombre de usuario debe tener entre 3 y 40 caracteres");
        }
        if (correo != null && !correo.isBlank()
                && (correo.length() > 254 || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
            throw new ReglaNegocioException("El correo electrónico no tiene un formato válido");
        }
        if (contrasena == null || contrasena.length() < 8 || contrasena.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ReglaNegocioException("La contraseña debe tener entre 8 y 72 caracteres");
        }
    }

    private void verificarDisponibilidadDeNombreYCorreo(String nombre, String correo) {
        if (usuarioRepository.existsByNombreUsuario(nombre.trim())) throw new ReglaNegocioException("El nombre de usuario ya está en uso");
        String correoNormalizado = normalizarCorreo(correo);
        if (correoNormalizado != null && usuarioRepository.existsByCorreoElectronico(correoNormalizado)) {
            throw new ReglaNegocioException("El correo electrónico ya está en uso");
        }
    }

    private String generarHashDeContrasena(String contrasena) {
        return passwordEncoder.encode(contrasena);
    }

    private boolean verificarContrasenaIngresada(String contrasenaIngresada, String hashAlmacenado) {
        return passwordEncoder.matches(contrasenaIngresada, hashAlmacenado);
    }

    private String normalizarCorreo(String correo) {
        return correo == null || correo.isBlank() ? null : correo.trim().toLowerCase();
    }
}
