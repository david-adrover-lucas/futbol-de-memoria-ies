package com.auditoriria_ies.futbol_memoria.controllers;

import com.auditoriria_ies.futbol_memoria.services.TandaPenalesServices;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/usuarios/{usuarioId}/partidos/{partidoId}/penales")
public class TandaPenalesController {
    private final TandaPenalesServices service;
    public TandaPenalesController(TandaPenalesServices service) { this.service = service; }

    @PostMapping
    public TandaPenalesServices.TandaPenalVista iniciar(@PathVariable Long usuarioId, @PathVariable Long partidoId) {
        return service.iniciarObtener(usuarioId, partidoId);
    }

    @GetMapping
    public TandaPenalesServices.TandaPenalVista obtener(@PathVariable Long usuarioId, @PathVariable Long partidoId) {
        return service.obtener(usuarioId, partidoId);
    }

    @PostMapping("/respuestas")
    public TandaPenalesServices.TandaPenalVista responder(@PathVariable Long usuarioId, @PathVariable Long partidoId,
                                                          @Valid @RequestBody ApiRequests.RespuestaPenal respuesta) {
        return service.responder(usuarioId, partidoId, respuesta.preguntaId(), respuesta.opcionRespuestaId(),
                respuesta.textoRespuesta(), respuesta.segundos());
    }
}
