package com.auditoriria_ies.futbol_memoria.controllers;

import com.auditoriria_ies.futbol_memoria.models.Carrera;
import com.auditoriria_ies.futbol_memoria.services.CarreraServices;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api")
public class CarreraController {
    private final CarreraServices service;
    public CarreraController(CarreraServices service) { this.service = service; }
    @PostMapping("/usuarios/{usuarioId}/carrera") @ResponseStatus(HttpStatus.CREATED)
    public Carrera crear(@PathVariable Long usuarioId, @Valid @RequestBody ApiRequests.CrearCarrera b) {
        return service.crearCarreraParaUsuario(usuarioId,b.nombreDt(),b.apellidoDt(),b.edadDt(),b.provincia(),b.numeroAvatar(),b.equipoInicialId());
    }
    @GetMapping("/usuarios/{usuarioId}/carrera") public Carrera obtener(@PathVariable Long usuarioId) { return service.obtenerCarreraActivaPorUsuario(usuarioId); }
    @PutMapping("/carreras/{carreraId}/equipo") public Carrera equipo(@PathVariable Long carreraId,@Valid @RequestBody ApiRequests.CambiarEquipo b) { return service.cambiarEquipoActual(carreraId,b.equipoId()); }
}
