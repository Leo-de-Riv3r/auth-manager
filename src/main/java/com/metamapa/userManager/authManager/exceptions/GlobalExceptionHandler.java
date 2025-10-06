package com.metamapa.userManager.authManager.exceptions;

import jakarta.persistence.EntityExistsException;
import java.security.SignatureException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<String> handleUsernameNotFound(UsernameNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

  @ExceptionHandler(EntityExistsException.class)
  public ResponseEntity<Map<String, String>> handleDuplicatedUsername(EntityExistsException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",ex.getMessage()));
  }

//  @ExceptionHandler(Exception.class)
//  public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
//    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//        Map.of(
//            "timestamp", new Date(),
//            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
//            "error", "Error de autenticacion",
//            "message", ex.getMessage()
//        )
//    );
//  }
}
