package com.demandworkbench.requirement.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublishTaskRecord {
    private Long id;
    private String taskNo;
    private String requirementNo;
    private String status;
    private String title;
    private String category;
    private String summary;
    private String skills;
    private String experience;
    private int recruitCount;
    private String priceModel;
    private String budget;
    private LocalDate startDate;
    private LocalDate deadline;
    private String workMode;
    private String recruitMode;
    private String visibility;
    private LocalDateTime publishedAt;
}
