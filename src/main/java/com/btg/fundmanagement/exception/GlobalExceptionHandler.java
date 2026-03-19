package com.btg.fundmanagement.exception;

import com.btg.fundmanagement.dto.Responses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Responses.Error> handleApiException(ApiException ex) {
        var error = new Responses.Error(ex.getMessage(), ex.status(), Instant.now().toString());
        return ResponseEntity.status(ex.status()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Responses.Error> handleValidation(MethodArgumentNotValidException ex) {
        var message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        var error = new Responses.Error(message, 400, Instant.now().toString());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Responses.Error> handleGeneral(Exception ex) {
        var error = new Responses.Error("Error interno del servidor", 500, Instant.now().toString());
        return ResponseEntity.internalServerError().body(error);
    }
}
