package com.auditoriria_ies.futbol_memoria.controllers;

import com.auditoriria_ies.futbol_memoria.services.EstadisticaServices;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class EstadisticaController {
    private final EstadisticaServices service;
    public EstadisticaController(EstadisticaServices service) { this.service = service; }
    @GetMapping("/usuarios/{usuarioId}/estadisticas")
    public Map<String, Long> estadisticas(@PathVariable Long usuarioId) { return service.obtenerEstadisticasDeCarrera(usuarioId); }
    @GetMapping("/usuarios/{usuarioId}/historial")
    public Object historial(@PathVariable Long usuarioId, @RequestParam(required=false) Long competicionId) {
        return service.obtenerHistorialDePartidos(usuarioId, competicionId);
    }
    @GetMapping("/ranking")
    public Object ranking(@RequestParam(defaultValue="0") int pagina, @RequestParam(defaultValue="20") int tamano) {
        return service.listarRankingGlobal(pagina, tamano);
    }
}
