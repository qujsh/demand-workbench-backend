package com.demandworkbench.decomposition.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
        List<WorkItemResponse> children) {}
