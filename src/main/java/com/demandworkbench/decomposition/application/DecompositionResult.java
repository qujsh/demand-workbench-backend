package com.demandworkbench.decomposition.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DecompositionResult(
        String taskId,
        String status,
        String stage,
        String message,
        Summary summary,
        List<WorkItem> modules,
        String errorCode,
        LocalDateTime createdAt,
        LocalDateTime completedAt) {

    public record Summary(int moduleCount, int featureCount, int workItemCount, int subtaskCount) {}

    public record WorkItem(
            String id,
            String type,
            String name,
            String description,
            String status,
            String role,
            String priority,
            LocalDate startDate,
            LocalDate endDate,
            String dependency,
            boolean selected,
            List<WorkItem> children) {}
}
