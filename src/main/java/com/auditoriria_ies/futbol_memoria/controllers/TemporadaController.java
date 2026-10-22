package com.auditoriria_ies.futbol_memoria.controllers;

import com.auditoriria_ies.futbol_memoria.services.TemporadaServices;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/temporada")
public class TemporadaController {
    private final TemporadaServices service;
    public TemporadaController(TemporadaServices service) { this.service = service; }

    @GetMapping
    public TemporadaServices.TemporadaVista obtener(@PathVariable Long usuarioId) { return service.obtenerTemporada(usuarioId); }

    @PostMapping("/partido-siguiente")
    public TemporadaServices.FixtureVista siguiente(@PathVariable Long usuarioId) { return service.obtenerPartidoSiguiente(usuarioId); }
}
