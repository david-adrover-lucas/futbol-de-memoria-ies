package com.auditoriria_ies.futbol_memoria.controllers;
import com.auditoriria_ies.futbol_memoria.models.Pregunta;
import com.auditoriria_ies.futbol_memoria.services.PreguntaServices;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/preguntas")
public class PreguntaController {
 private final PreguntaServices service;
 public PreguntaController(PreguntaServices service){this.service=service;}
 @GetMapping("/{id}") public Pregunta obtener(@PathVariable Long id){return service.obtenerPreguntaConOpcionesParaPartido(id);}
}
