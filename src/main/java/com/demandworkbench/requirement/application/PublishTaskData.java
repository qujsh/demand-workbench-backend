package com.demandworkbench.requirement.application;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PublishTaskData(
        Long id, String taskNo, String requirementNo, String decompositionNo, String status, String title, String category,
        String summary, String skills, String experience, int recruitCount, String priceModel,
        String budget, LocalDate startDate, LocalDate deadline, String workMode, String recruitMode,
        String visibility, LocalDateTime publishedAt) {
}
