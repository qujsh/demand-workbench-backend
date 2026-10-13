package com.demandworkbench.decomposition.application;

import java.time.LocalDate;

public record WorkItemData(
        String workItemNo,
        String decompositionNo,
        String parentWorkItemNo,
        String type,
        String name,
        String description,
        String status,
        String role,
        String priority,
        LocalDate startDate,
        LocalDate endDate,
        String dependency) {
}
