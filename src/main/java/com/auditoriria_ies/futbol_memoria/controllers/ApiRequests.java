package com.auditoriria_ies.futbol_memoria.controllers;

import jakarta.validation.constraints.*;

public final class ApiRequests {
    private ApiRequests() {}
    public record Registro(@NotBlank String nombreUsuario, String correoElectronico, @NotBlank String contrasena) {}
    public record Login(@NotBlank String nombreUsuario, @NotBlank String contrasena) {}
    public record Perfil(@NotBlank String nombreUsuario, String correoElectronico) {}
    public record CrearCarrera(@NotBlank String nombreDt, @NotBlank String apellidoDt,
            @NotNull Integer edadDt, @NotBlank String provincia, @NotNull Integer numeroAvatar,
            @NotNull Long equipoInicialId) {}
    public record CambiarEquipo(@NotNull Long equipoId) {}
    public record Respuesta(@NotNull Long preguntaId, Long opcionRespuestaId, String textoRespuesta,
            @NotNull @Min(0) @Max(15) Integer segundos) {}
    public record RespuestaPenal(@NotNull Long preguntaId, Long opcionRespuestaId, String textoRespuesta,
            @NotNull @Min(0) @Max(15) Integer segundos) {}
    public record Comodin(@NotBlank String tipoComodin) {}
}
