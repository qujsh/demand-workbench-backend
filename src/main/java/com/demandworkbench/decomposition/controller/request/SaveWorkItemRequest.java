package com.demandworkbench.decomposition.controller.request;

import com.demandworkbench.decomposition.application.WorkItemData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SaveWorkItemRequest(
        @Size(max = 96) String workItemNo,
        @NotBlank @Size(max = 40) String decompositionNo,
        @Size(max = 96) String parentWorkItemNo,
        @NotBlank @Pattern(regexp = "module|feature|work|subtask") String type,
        @NotBlank @Size(max = 255) String name,
        String description,
        @NotBlank @Size(max = 24) String status,
        @Size(max = 64) String role,
        @Size(max = 16) String priority,
        LocalDate startDate,
        LocalDate endDate,
        @Size(max = 500) String dependency) {

    public WorkItemData toData() {
        return new WorkItemData(workItemNo, decompositionNo, parentWorkItemNo, type, name, description,
                status, role, priority, startDate, endDate, dependency);
    }
}
