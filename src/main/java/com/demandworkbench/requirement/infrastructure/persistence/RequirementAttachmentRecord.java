package com.demandworkbench.requirement.infrastructure.persistence;

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
}
