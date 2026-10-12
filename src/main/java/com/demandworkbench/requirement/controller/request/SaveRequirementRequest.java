package com.demandworkbench.requirement.controller.request;

import com.demandworkbench.requirement.application.RequirementData;
import com.demandworkbench.requirement.application.RequirementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SaveRequirementRequest(
        @NotBlank(message = "需求标题不能为空")
        @Size(max = 255, message = "需求标题不能超过 255 个字符")
        String title,
        String userDescription,
        String aiDescription,
        @NotBlank(message = "需求阶段不能为空")
        String stage,
        @Size(max = 40, message = "拆解任务编号不能超过 40 个字符")
        String currentDecompositionId,
        @Valid @NotNull(message = "附件列表不能为空")
        List<AttachmentRequest> attachments) {

    public RequirementService.SaveCommand toCommand() {
        var attachmentData = attachments.stream()
                .map(item -> new RequirementData.AttachmentData(null, item.fileName(), item.storageKey(),
                        item.contentType(), item.fileSize(), item.sortOrder()))
                .toList();
        return new RequirementService.SaveCommand(title, userDescription, aiDescription, stage,
                currentDecompositionId, attachmentData);
    }

    public record AttachmentRequest(
            @NotBlank(message = "附件文件名不能为空")
            @Size(max = 255, message = "附件文件名不能超过 255 个字符")
            String fileName,
            @NotBlank(message = "附件存储标识不能为空")
            @Size(max = 500, message = "附件存储标识不能超过 500 个字符")
            String storageKey,
            @Size(max = 128, message = "附件媒体类型不能超过 128 个字符")
            String contentType,
            @Min(value = 0, message = "附件大小不能小于 0")
            long fileSize,
            @Min(value = 0, message = "附件顺序不能小于 0")
            int sortOrder) {
    }
}
