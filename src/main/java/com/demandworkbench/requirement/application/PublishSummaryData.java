package com.demandworkbench.requirement.application;

import java.time.LocalDateTime;

public record PublishSummaryData(
        String requirementNo,
        String status,
        String content,
        String promptVersion,
        LocalDateTime generatedAt) {
}
