package com.auditoriria_ies.futbol_memoria.controllers;
import com.auditoriria_ies.futbol_memoria.models.NivelDificultad;
import com.auditoriria_ies.futbol_memoria.services.NivelDificultadServices;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/niveles-dificultad")
public class NivelDificultadController {
 private final NivelDificultadServices service;
 public NivelDificultadController(NivelDificultadServices service){this.service=service;}
 @GetMapping public List<NivelDificultad> listar(){return service.listarNivelesDeDificultad();}
 @GetMapping("/{numero}") public NivelDificultad obtener(@PathVariable Integer numero){return service.obtenerNivelDeDificultadPorNumero(numero);}
}
