package br.com.king.flick_business.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

  // Exceção personalizada para recurso não encontrado
  @ExceptionHandler(RecursoNaoEncontrado.class)
  public ResponseEntity<Object> handleRecursoNaoEncontradoException(RecursoNaoEncontrado ex,
      WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("message", ex.getMessage());
    body.put("status", HttpStatus.NOT_FOUND.value());
    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
  }

  // Exceção para recurso não deletável
  @ExceptionHandler(RecursoNaoDeletavel.class)
  public ResponseEntity<Object> handleRecursoNaoDeletavelException(RecursoNaoDeletavel ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("message", ex.getMessage());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  // Exceção para recurso já cadastrado
  @ExceptionHandler(RecursoJaCadastrado.class)
  public ResponseEntity<Object> handleRecursoJaCadastradoException(RecursoJaCadastrado ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("message", ex.getMessage());
    body.put("status", HttpStatus.CONFLICT.value());
    return new ResponseEntity<>(body, HttpStatus.CONFLICT);
  }

  // Exceção genérica
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("message", "An unexpected error occurred");
    body.put("details", ex.getMessage());
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}