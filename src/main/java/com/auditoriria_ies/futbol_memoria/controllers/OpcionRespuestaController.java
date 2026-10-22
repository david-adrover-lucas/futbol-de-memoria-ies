package com.auditoriria_ies.futbol_memoria.controllers;
import com.auditoriria_ies.futbol_memoria.models.OpcionRespuesta;
import com.auditoriria_ies.futbol_memoria.services.OpcionRespuestaServices;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/preguntas/{preguntaId}/opciones")
public class OpcionRespuestaController {
 private final OpcionRespuestaServices service;
 public OpcionRespuestaController(OpcionRespuestaServices service){this.service=service;}
 @GetMapping public List<OpcionRespuesta> listar(@PathVariable Long preguntaId){return service.listarOpcionesDePregunta(preguntaId);}
 @GetMapping("/{opcionId}") public OpcionRespuesta validar(@PathVariable Long preguntaId,@PathVariable Long opcionId){return service.validarOpcionDeRespuesta(preguntaId,opcionId);}
}
