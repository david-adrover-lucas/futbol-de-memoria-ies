package com.auditoriria_ies.futbol_memoria.controllers;

import com.auditoriria_ies.futbol_memoria.services.CarreraTorneoServices;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/torneos/{codigo}")
public class CarreraTorneoController {
    private final CarreraTorneoServices service;
    public CarreraTorneoController(CarreraTorneoServices service) { this.service = service; }

    @PostMapping
    public CarreraTorneoServices.TorneoVista inscribirse(@PathVariable Long usuarioId, @PathVariable String codigo) {
        return service.iniciarObtener(usuarioId, codigo);
    }
    @GetMapping
    public CarreraTorneoServices.TorneoVista obtener(@PathVariable Long usuarioId, @PathVariable String codigo) {
        return service.obtener(usuarioId, codigo);
    }
    @PostMapping("/partido")
    public CarreraTorneoServices.FixtureVista siguientePartido(@PathVariable Long usuarioId, @PathVariable String codigo) {
        return service.siguientePartido(usuarioId, codigo);
    }
}
