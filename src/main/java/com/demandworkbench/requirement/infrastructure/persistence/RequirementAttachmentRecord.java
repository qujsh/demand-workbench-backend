package com.demandworkbench.requirement.infrastructure.persistence;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequirementAttachmentRecord {
    private Long id;
    private Long requirementId;
    private String fileName;
    private String storageKey;
    private String contentType;
    private long fileSize;
    private int sortOrder;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
