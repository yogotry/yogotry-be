package com.yogotry.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of("code", 400, "message", e.getMessage(), "data", ""));
    }

    @ExceptionHandler(InvalidIdTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidIdToken(InvalidIdTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("code", 401, "message", e.getMessage(), "data", ""));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Map.of("code", 403, "message", e.getMessage(), "data", ""));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleInternalServerError(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("code", 500, "message", "Internal Server Error", "data", ""));
    }
}
