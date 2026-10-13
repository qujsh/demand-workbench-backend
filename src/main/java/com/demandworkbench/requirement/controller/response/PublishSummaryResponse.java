package com.demandworkbench.requirement.controller.response;

import com.demandworkbench.requirement.application.PublishSummaryData;
import java.time.LocalDateTime;

public record PublishSummaryResponse(
        String requirementNo,
        String status,
        String content,
        String promptVersion,
        LocalDateTime generatedAt) {

    public static PublishSummaryResponse from(PublishSummaryData data) {
        return new PublishSummaryResponse(data.requirementNo(), data.status(), data.content(),
                data.promptVersion(), data.generatedAt());
    }
}
