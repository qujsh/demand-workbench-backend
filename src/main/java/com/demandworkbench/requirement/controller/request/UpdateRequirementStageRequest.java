package com.demandworkbench.requirement.controller.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateRequirementStageRequest(
        @NotBlank(message = "需求阶段不能为空") String stage) {
}
