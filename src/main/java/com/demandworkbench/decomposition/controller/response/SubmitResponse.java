package com.demandworkbench.decomposition.controller.response;

import com.demandworkbench.decomposition.application.SubmitResult;

public record SubmitResponse(String taskId, String status, String message) {
    public static SubmitResponse from(SubmitResult result) {
        return new SubmitResponse(result.taskId(), result.status(), result.message());
    }
}
