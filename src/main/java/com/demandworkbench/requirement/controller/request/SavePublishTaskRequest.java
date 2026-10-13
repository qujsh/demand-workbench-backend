package com.demandworkbench.requirement.controller.request;

import com.demandworkbench.requirement.application.PublishTaskData;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record SavePublishTaskRequest(
        @Size(max = 24) String status, @Size(max = 255) String title, @Size(max = 64) String category,
        @Size(max = 5000) String summary, @Size(max = 500) String skills,
        @Size(max = 32) String experience, @Min(0) int recruitCount,
        @Size(max = 32) String priceModel, @Size(max = 500) String budget,
        LocalDate startDate, LocalDate deadline, @Size(max = 32) String workMode,
        @Size(max = 32) String recruitMode, @Size(max = 32) String visibility,
        @NotNull List<@Size(max = 96) String> workItemNos) {

    public PublishTaskData toData(String requirementNo) {
        return new PublishTaskData(null, null, requirementNo, text(status), text(title), text(category),
                text(summary), text(skills), text(experience), recruitCount, text(priceModel), text(budget),
                startDate, deadline, text(workMode), text(recruitMode), text(visibility), workItemNos, null);
    }

    private String text(String value) {
        return value == null ? "" : value;
    }
}
