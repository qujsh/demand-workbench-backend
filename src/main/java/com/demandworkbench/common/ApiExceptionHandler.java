package com.demandworkbench.common;

import com.demandworkbench.decomposition.application.DecompositionException;
import com.demandworkbench.decomposition.application.DecompositionErrorCode;
import com.demandworkbench.requirement.application.RequirementException;
import com.demandworkbench.requirement.application.RequirementErrorCode;
import com.demandworkbench.requirement.controller.request.SaveRequirementRequest;
import com.demandworkbench.requirement.controller.request.SavePublishTaskRequest;
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

    @ExceptionHandler(RequirementException.class)
    ResponseEntity<ApiResponse<Void>> handleRequirement(RequirementException exception) {
        var errorCode = exception.getErrorCode();
        var status = switch (errorCode) {
            case INVALID_REQUIREMENT, INVALID_PUBLISH_TASK, PUBLISH_TASK_CANCEL_FAILED -> HttpStatus.BAD_REQUEST;
            case REQUIREMENT_NOT_FOUND, DECOMPOSITION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DECOMPOSITION_NOT_COMPLETED -> HttpStatus.CONFLICT;
            case PUBLISH_SUMMARY_SOURCE_EMPTY -> HttpStatus.BAD_REQUEST;
            case REQUIREMENT_SAVE_FAILED, PUBLISH_SUMMARY_GENERATION_FAILED, PUBLISH_TASK_SAVE_FAILED ->
                    HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status)
                .body(ApiResponse.failure(errorCode.getCode(), exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ResponseEntity<ApiResponse<Void>> handleValidation(Exception exception) {
        var message = exception instanceof MethodArgumentNotValidException validation
                ? validation.getBindingResult().getAllErrors().getFirst().getDefaultMessage()
                : exception.getMessage();
        if (exception instanceof MethodArgumentNotValidException validation
                && validation.getBindingResult().getTarget() instanceof SaveRequirementRequest) {
            var errorCode = RequirementErrorCode.INVALID_REQUIREMENT;
            return ResponseEntity.badRequest().body(ApiResponse.failure(errorCode.getCode(), message));
        }
        if (exception instanceof MethodArgumentNotValidException validation
                && validation.getBindingResult().getTarget() instanceof SavePublishTaskRequest) {
            var errorCode = RequirementErrorCode.INVALID_PUBLISH_TASK;
            return ResponseEntity.badRequest().body(ApiResponse.failure(errorCode.getCode(), message));
        }
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
