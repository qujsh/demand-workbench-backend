package com.demandworkbench.common;

import com.demandworkbench.decomposition.application.DecompositionException;
import com.demandworkbench.decomposition.application.DecompositionErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {
    @ExceptionHandler(DecompositionException.class)
    ResponseEntity<ApiResponse<Void>> handleDecomposition(DecompositionException exception) {
        var errorCode = exception.getErrorCode();
        return ResponseEntity.status(statusOf(errorCode))
                .body(ApiResponse.failure(errorCode.getCode(), exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ResponseEntity<ApiResponse<Void>> handleValidation(Exception exception) {
        var message = exception instanceof MethodArgumentNotValidException validation
                ? validation.getBindingResult().getAllErrors().getFirst().getDefaultMessage()
                : exception.getMessage();
        var errorCode = DecompositionErrorCode.INVALID_CONTENT;
        return ResponseEntity.badRequest().body(ApiResponse.failure(errorCode.getCode(), message));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        log.error("Unhandled API exception", exception);
        var errorCode = DecompositionErrorCode.INTERNAL_ERROR;
        return ResponseEntity.internalServerError()
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMsg()));
    }

    private HttpStatus statusOf(DecompositionErrorCode errorCode) {
        return switch (errorCode) {
            case TASK_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case TASK_QUEUE_FULL -> HttpStatus.SERVICE_UNAVAILABLE;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
