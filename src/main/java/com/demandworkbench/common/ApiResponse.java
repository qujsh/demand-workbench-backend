package com.demandworkbench.common;

public record ApiResponse<T>(
        boolean success,
        T msg,
        String errorCode,
        String errorMsg) {

    public static <T> ApiResponse<T> success(T msg) {
        return new ApiResponse<>(true, msg, "", "");
    }

    public static ApiResponse<Void> failure(String errorCode, String errorMsg) {
        return new ApiResponse<>(false, null, errorCode, errorMsg);
    }
}
