package com.demandworkbench.decomposition.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DeleteWorkItemsRequest(
        @NotEmpty(message = "请选择要删除的工作项")
        @Size(max = 500, message = "单次最多删除 500 个工作项")
        List<@NotBlank(message = "工作项编号不能为空") String> workItemNos) {
}
