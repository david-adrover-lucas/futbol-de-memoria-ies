package com.auditoriria_ies.futbol_memoria.controllers;
import com.auditoriria_ies.futbol_memoria.models.*;
import com.auditoriria_ies.futbol_memoria.services.PartidoServices;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@RestController @Validated @RequestMapping("/api/usuarios/{usuarioId}/partidos")
public class PartidoController {
 private final PartidoServices service;
 public PartidoController(PartidoServices service){this.service=service;}
 public record Iniciar(@NotBlank String codigoCompeticion, Long temporadaFixtureId, Long torneoFixtureId){}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Partido iniciar(@PathVariable Long usuarioId,@Valid @RequestBody Iniciar b){return service.iniciarPartido(usuarioId,b.codigoCompeticion(),b.temporadaFixtureId(),b.torneoFixtureId());}
 @GetMapping("/{partidoId}") public Partido estado(@PathVariable Long usuarioId,@PathVariable Long partidoId){return service.obtenerEstadoActualDelPartido(usuarioId,partidoId);}
 @GetMapping("/{partidoId}/pregunta-actual") public Optional<PartidoPregunta> pregunta(@PathVariable Long usuarioId,@PathVariable Long partidoId){return service.obtenerPreguntaActualDelPartido(usuarioId,partidoId);}
 @GetMapping("/{partidoId}/opciones") public List<OpcionRespuesta> opciones(@PathVariable Long usuarioId,@PathVariable Long partidoId){return service.obtenerOpcionesDeLaPreguntaActual(usuarioId,partidoId);}
 @PostMapping("/{partidoId}/respuestas") public Partido responder(@PathVariable Long usuarioId,@PathVariable Long partidoId,@Valid @RequestBody ApiRequests.Respuesta b){return service.registrarRespuesta(usuarioId,partidoId,b.preguntaId(),b.opcionRespuestaId(),b.textoRespuesta(),b.segundos());}
 @PostMapping("/{partidoId}/comodines") public PartidoPregunta comodin(@PathVariable Long usuarioId,@PathVariable Long partidoId,@Valid @RequestBody ApiRequests.Comodin b){return service.usarComodin(usuarioId,partidoId,b.tipoComodin());}
 @GetMapping("/{partidoId}/comodines") public List<String> comodinesUsados(@PathVariable Long usuarioId,@PathVariable Long partidoId){return service.listarTiposDeComodinUsados(usuarioId,partidoId);}
 @PostMapping("/{partidoId}/finalizar") public Partido finalizar(@PathVariable Long usuarioId,@PathVariable Long partidoId){return service.finalizarPartido(usuarioId,partidoId);}
}
