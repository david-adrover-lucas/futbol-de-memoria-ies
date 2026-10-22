package com.auditoriria_ies.futbol_memoria.controllers;
import com.auditoriria_ies.futbol_memoria.models.IntentoPartido;
import com.auditoriria_ies.futbol_memoria.services.IntentoPartidoServices;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/usuarios/{usuarioId}/partidos/{partidoId}/intentos")
public class IntentoPartidoController {
 private final IntentoPartidoServices service;
 public IntentoPartidoController(IntentoPartidoServices service){this.service=service;}
 @GetMapping public List<IntentoPartido> listar(@PathVariable Long usuarioId,@PathVariable Long partidoId){return service.listarIntentosDePartido(usuarioId,partidoId);}
}
