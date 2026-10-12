package com.demandworkbench.decomposition.controller.response;

import com.demandworkbench.decomposition.application.DecompositionResult;
import java.time.LocalDateTime;
import java.util.List;

public record TaskResponse(
        String taskId,
        String status,
        String stage,
        String message,
        Summary summary,
        List<WorkItemResponse> modules,
        String errorCode,
        LocalDateTime createdAt,
        LocalDateTime completedAt) {

    public static TaskResponse from(DecompositionResult result) {
        var summary = result.summary() == null ? null : new Summary(
                result.summary().moduleCount(), result.summary().featureCount(),
                result.summary().workItemCount(), result.summary().subtaskCount());
        return new TaskResponse(result.taskId(), result.status(), result.stage(), result.message(), summary,
                result.modules().stream().map(WorkItemResponse::from).toList(), result.errorCode(),
                result.createdAt(), result.completedAt());
    }

    public record Summary(int moduleCount, int featureCount, int workItemCount, int subtaskCount) {}
}
