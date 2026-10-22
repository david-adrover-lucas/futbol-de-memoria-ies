package com.auditoriria_ies.futbol_memoria.controllers;

import com.auditoriria_ies.futbol_memoria.services.AutenticacionServices;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {
    private final AutenticacionServices service;
    public AutenticacionController(AutenticacionServices service) { this.service = service; }
    @PostMapping("/registro") @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> registrar(@Valid @RequestBody ApiRequests.Registro body) {
        return Map.of("usuarioId", service.registrarUsuario(body.nombreUsuario(), body.correoElectronico(), body.contrasena()));
    }
    @PostMapping("/login")
    public Map<String, Long> login(@Valid @RequestBody ApiRequests.Login body) {
        return Map.of("usuarioId", service.iniciarSesion(body.nombreUsuario(), body.contrasena()));
    }
}
