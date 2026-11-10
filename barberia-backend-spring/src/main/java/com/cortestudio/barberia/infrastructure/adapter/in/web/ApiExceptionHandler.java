package com.cortestudio.barberia.infrastructure.adapter.in.web;

import com.cortestudio.barberia.domain.exception.ForbiddenException;
import com.cortestudio.barberia.domain.exception.NotFoundException;
import com.cortestudio.barberia.domain.exception.UnauthorizedException;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** ERRORES HTTP: centraliza excepciones y las convierte en respuestas REST coherentes. */
@RestControllerAdvice
public class ApiExceptionHandler {
    // SOLICITUD INCORRECTA: campos o parámetros enviados no válidos (HTTP 400).
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    // VALIDACIÓN DEL BODY: responde cuando @Valid detecta datos incompletos o incorrectos.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalidBody() {
        return response(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos");
    }

    // CONFLICTO: MongoDB rechazó una clave única duplicada, por ejemplo un horario repetido.
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, String>> conflict() {
        return response(HttpStatus.CONFLICT, "Ese horario ya está ocupado o el correo ya está registrado");
    }

    // RECURSO INEXISTENTE (HTTP 404).
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(NotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    // AUTENTICACIÓN REQUERIDA O TOKEN INVÁLIDO (HTTP 401).
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> unauthorized(UnauthorizedException exception) {
        return response(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    // AUTORIZACIÓN: el usuario está identificado, pero no tiene permiso (HTTP 403).
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> forbidden(ForbiddenException exception) {
        return response(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    // FORMATO UNIFICADO: todas las respuestas de error contienen un campo "error".
    private ResponseEntity<Map<String, String>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message == null ? "Error en la solicitud" : message));
    }
}
