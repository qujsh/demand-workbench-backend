package com.demandworkbench.decomposition.infrastructure.persistence;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DecompositionRecord {
    private Long id;
    private String decompositionNo;
    private String sourceContent;
    private String status;
    private String stage;
    private String model;
    private String promptVersion;
    private String rawResponse;
    private String errorCode;
    private String errorMessage;
    private Integer promptTokens;
    private Integer completionTokens;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

}
