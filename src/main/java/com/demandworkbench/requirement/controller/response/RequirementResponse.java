package com.demandworkbench.requirement.controller.response;

import com.demandworkbench.requirement.application.RequirementData;
import java.time.LocalDateTime;
import java.util.List;

public record RequirementResponse(
        String requirementNo,
        String title,
        String userDescription,
        String aiDescription,
        String stage,
        String currentDecompositionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int moduleCount,
        int workItemCount,
        List<AttachmentResponse> attachments) {

    public static RequirementResponse from(RequirementData data) {
        return new RequirementResponse(data.requirementNo(), data.title(), data.userDescription(),
                data.aiDescription(), data.stage(), data.currentDecompositionId(), data.createdAt(), data.updatedAt(),
                data.moduleCount(), data.workItemCount(),
                data.attachments().stream().map(AttachmentResponse::from).toList());
    }

    public record AttachmentResponse(
            long id,
            String fileName,
            String storageKey,
            String contentType,
            long fileSize,
            int sortOrder) {
        private static AttachmentResponse from(RequirementData.AttachmentData data) {
            return new AttachmentResponse(data.id(), data.fileName(), data.storageKey(), data.contentType(),
                    data.fileSize(), data.sortOrder());
        }
    }
}
