package com.demandworkbench.common;

import com.demandworkbench.decomposition.application.DecompositionException;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(DecompositionException.class)
    ResponseEntity<Map<String, String>> handleDecomposition(DecompositionException exception) {
        var status = "TASK_NOT_FOUND".equals(exception.getCode()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("code", exception.getCode(), "message", exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ResponseEntity<Map<String, String>> handleValidation(Exception exception) {
        var message = exception instanceof MethodArgumentNotValidException validation
                ? validation.getBindingResult().getAllErrors().getFirst().getDefaultMessage()
                : exception.getMessage();
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_CONTENT", "message", message));
    }
}
