package com.demandworkbench.decomposition.application;

public class DecompositionException extends RuntimeException {
    private final DecompositionErrorCode errorCode;

    public DecompositionException(DecompositionErrorCode errorCode) {
        this(errorCode, errorCode.getMsg());
    }

    public DecompositionException(DecompositionErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DecompositionException(DecompositionErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public DecompositionErrorCode getErrorCode() {
        return errorCode;
    }
}
