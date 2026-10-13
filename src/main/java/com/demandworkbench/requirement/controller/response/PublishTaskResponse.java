package com.demandworkbench.requirement.controller.response;

import com.demandworkbench.requirement.application.PublishTaskData;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PublishTaskResponse(
        String taskNo, String requirementNo, String status, String title, String category, String summary,
        String skills, String experience, int recruitCount, String priceModel, String budget,
        LocalDate startDate, LocalDate deadline, String workMode, String recruitMode, String visibility,
        List<String> workItemNos, LocalDateTime publishedAt) {
    public static PublishTaskResponse from(PublishTaskData data) {
        if (data == null) return null;
        return new PublishTaskResponse(data.taskNo(), data.requirementNo(), data.status(), data.title(),
                data.category(), data.summary(), data.skills(), data.experience(), data.recruitCount(),
                data.priceModel(), data.budget(), data.startDate(), data.deadline(), data.workMode(),
                data.recruitMode(), data.visibility(), data.workItemNos(), data.publishedAt());
    }
}
