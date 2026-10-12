package com.demandworkbench.requirement.application;

import java.time.LocalDateTime;
import java.util.List;

public record RequirementData(
        Long id,
        String requirementNo,
        String titleCode,
        String title,
        String userDescription,
        String aiDescription,
        String stage,
        String currentDecompositionNo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int moduleCount,
        int workItemCount,
        List<AttachmentData> attachments) {

    public record AttachmentData(
            Long id,
            String fileName,
            String storageKey,
            String contentType,
            long fileSize,
            int sortOrder) {
    }
}
