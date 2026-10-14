package com.demandworkbench.requirement.infrastructure.persistence;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequirementRecord {
    private Long id;
    private Long ownerUserId;
    private String requirementNo;
    private String titleCode;
    private String title;
    private String userDescription;
    private String aiDescription;
    private String publishSummary;
    private String publishSummaryStatus;
    private String publishSummaryPromptVersion;
    private LocalDateTime publishSummaryGeneratedAt;
    private String stage;
    private Long currentDecompositionId;
    private String currentDecompositionNo;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int moduleCount;
    private int workItemCount;
}
