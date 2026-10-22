package com.auditoriria_ies.futbol_memoria.controllers;
import com.auditoriria_ies.futbol_memoria.models.Equipo;
import com.auditoriria_ies.futbol_memoria.services.EquipoServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/equipos")
public class EquipoController {
 private final EquipoServices service;
 public EquipoController(EquipoServices service){this.service=service;}
 @GetMapping public Page<Equipo> listar(@RequestParam(required=false) String pais,@RequestParam(required=false) Integer prestigio,@RequestParam(required=false) Boolean internacional,@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamano){if(pagina<0||tamano<1||tamano>100)throw new IllegalArgumentException("Paginación inválida");return service.listarEquiposDisponibles(pais,prestigio,internacional,PageRequest.of(pagina,tamano));}
 @GetMapping("/seleccionables") public java.util.List<Equipo> seleccionables(@RequestParam(defaultValue="1") Integer estrellas){return service.listarEquiposSeleccionablesPorDirectorTecnico(estrellas);}
 @GetMapping("/{id}") public Equipo obtener(@PathVariable Long id){return service.obtenerEquipoPorId(id);}
}
