package com.demandworkbench.decomposition.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkItemRecord {
    private String id;
    private String decompositionId;
    private String parentId;
    private String type;
    private String name;
    private String description;
    private String status;
    private String roleName;
    private String priority;
    private LocalDate startDate;
    private LocalDate endDate;
    private String dependencyText;
    private int sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
