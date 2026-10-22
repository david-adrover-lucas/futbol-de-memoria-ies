package com.auditoriria_ies.futbol_memoria.controllers;
import com.auditoriria_ies.futbol_memoria.models.Usuario;
import com.auditoriria_ies.futbol_memoria.services.UsuarioServices;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/usuarios")
public class UsuarioController {
 private final UsuarioServices service;
 public UsuarioController(UsuarioServices service){this.service=service;}
 @GetMapping("/{id}") public Usuario perfil(@PathVariable Long id){return service.obtenerPerfilDeUsuario(id);}
 @PutMapping("/{id}") public Usuario actualizar(@PathVariable Long id,@Valid @RequestBody ApiRequests.Perfil b){return service.actualizarDatosDePerfil(id,b.nombreUsuario(),b.correoElectronico());}
}
