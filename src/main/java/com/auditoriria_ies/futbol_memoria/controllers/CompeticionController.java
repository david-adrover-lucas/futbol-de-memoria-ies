package com.auditoriria_ies.futbol_memoria.controllers;
import com.auditoriria_ies.futbol_memoria.models.Competicion;
import com.auditoriria_ies.futbol_memoria.services.CompeticionServices;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/competiciones")
public class CompeticionController {
 private final CompeticionServices service;
 public CompeticionController(CompeticionServices service){this.service=service;}
 @GetMapping public List<Competicion> listar(@RequestParam(required=false) String tipo){return service.listarCompeticionesDisponibles(tipo);}
 @GetMapping("/{codigo}") public Competicion obtener(@PathVariable String codigo){return service.obtenerCompeticionPorCodigo(codigo);}
}
