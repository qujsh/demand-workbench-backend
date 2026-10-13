package com.demandworkbench.requirement.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PublishTaskData(
        Long id, String taskNo, String requirementNo, String status, String title, String category,
        String summary, String skills, String experience, int recruitCount, String priceModel,
        String budget, LocalDate startDate, LocalDate deadline, String workMode, String recruitMode,
        String visibility, List<String> workItemNos, LocalDateTime publishedAt) {
}
