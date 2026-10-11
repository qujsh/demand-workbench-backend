package com.demandworkbench.decomposition.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DecomposeRequest(
        @NotBlank(message = "待拆解内容不能为空")
        @Size(max = 50_000, message = "待拆解内容不能超过50000个字符")
        String content) {}
