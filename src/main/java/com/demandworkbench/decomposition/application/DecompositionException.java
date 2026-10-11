package com.demandworkbench.decomposition.application;

public class DecompositionException extends RuntimeException {
    private final String code;

    public DecompositionException(String code, String message) {
        super(message);
        this.code = code;
    }

    public DecompositionException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() { return code; }
}
