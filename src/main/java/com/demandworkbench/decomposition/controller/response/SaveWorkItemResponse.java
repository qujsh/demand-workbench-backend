package com.demandworkbench.decomposition.controller.response;

import com.demandworkbench.decomposition.application.WorkItemData;
import java.time.LocalDate;

public record SaveWorkItemResponse(
        String id,
        String type,
        String name,
        String description,
        String status,
        String role,
        String priority,
        LocalDate startDate,
        LocalDate endDate,
        String dependency) {

    public static SaveWorkItemResponse from(WorkItemData data) {
        return new SaveWorkItemResponse(data.workItemNo(), data.type(), data.name(), data.description(),
                data.status(), data.role(), data.priority(), data.startDate(), data.endDate(), data.dependency());
    }
}
