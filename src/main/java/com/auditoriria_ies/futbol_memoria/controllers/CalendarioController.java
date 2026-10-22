package com.auditoriria_ies.futbol_memoria.controllers;

import com.auditoriria_ies.futbol_memoria.services.CalendarioServices;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/calendario")
public class CalendarioController {
    private final CalendarioServices service;
    public CalendarioController(CalendarioServices service) { this.service = service; }

    @GetMapping
    public CalendarioServices.CalendarioVista actual(@PathVariable Long usuarioId) {
        return service.obtenerCalendarioActual(usuarioId);
    }

    @PostMapping("/avanzar")
    public CalendarioServices.CalendarioVista avanzar(@PathVariable Long usuarioId) {
        return service.avanzarFechaLibre(usuarioId);
    }
}
