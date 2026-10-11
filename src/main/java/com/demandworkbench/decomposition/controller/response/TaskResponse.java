package com.demandworkbench.decomposition.controller.response;

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

    public record Summary(int moduleCount, int featureCount, int workItemCount, int subtaskCount) {}
}
