package com.demandworkbench.requirement.infrastructure.persistence;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequirementRecord {
    private Long id;
    private String requirementNo;
    private String titleCode;
    private String title;
    private String userDescription;
    private String aiDescription;
    private String stage;
    private String currentDecompositionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
