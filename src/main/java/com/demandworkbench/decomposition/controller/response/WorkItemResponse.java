package com.demandworkbench.decomposition.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.demandworkbench.decomposition.application.DecompositionResult;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record WorkItemResponse(
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
        List<WorkItemResponse> children) {

    public static WorkItemResponse from(DecompositionResult.WorkItem item) {
        return new WorkItemResponse(item.id(), item.type(), item.name(), item.description(), item.status(),
                item.role(), item.priority(), item.startDate(), item.endDate(), item.dependency(), item.selected(),
                item.children().stream().map(WorkItemResponse::from).toList());
    }
}
