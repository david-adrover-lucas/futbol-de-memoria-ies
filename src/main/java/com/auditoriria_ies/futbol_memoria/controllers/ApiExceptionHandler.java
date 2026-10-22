package com.auditoriria_ies.futbol_memoria.controllers;

import com.auditoriria_ies.futbol_memoria.exceptions.RecursoNoEncontradoException;
import com.auditoriria_ies.futbol_memoria.exceptions.ReglaNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.ConstraintViolationException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String,String>> notFound(RecursoNoEncontradoException e) { return response(HttpStatus.NOT_FOUND, e.getMessage()); }
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String,String>> business(ReglaNegocioException e) { return response(HttpStatus.BAD_REQUEST, e.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> invalid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream().findFirst().map(x -> x.getField() + ": dato obligatorio o inválido").orElse("Solicitud inválida");
        return response(HttpStatus.BAD_REQUEST, message);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,String>> conflict(DataIntegrityViolationException e) { return response(HttpStatus.CONFLICT, "La operación entra en conflicto con datos existentes"); }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> invalidArgument(IllegalArgumentException e) { return response(HttpStatus.BAD_REQUEST, e.getMessage()); }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String,String>> constraintViolation(ConstraintViolationException e) { return response(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos"); }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String,String>> resourceNotFound(NoResourceFoundException e) {
        return response(HttpStatus.NOT_FOUND, "No se encontró el recurso solicitado");
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> unexpected(Exception e) {
        logger.error("Error interno procesando una solicitud de la API", e);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno. Intenta nuevamente.");
    }
    private ResponseEntity<Map<String,String>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("estado", status.getReasonPhrase(), "mensaje", message));
    }
}
