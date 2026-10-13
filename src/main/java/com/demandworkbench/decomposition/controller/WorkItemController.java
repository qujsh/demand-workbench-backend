package com.demandworkbench.decomposition.controller;

import com.demandworkbench.common.ApiResponse;
import com.demandworkbench.decomposition.application.WorkItemCommandService;
import com.demandworkbench.decomposition.controller.request.DeleteWorkItemsRequest;
import com.demandworkbench.decomposition.controller.request.SaveWorkItemRequest;
import com.demandworkbench.decomposition.controller.response.DeleteWorkItemsResponse;
import com.demandworkbench.decomposition.controller.response.SaveWorkItemResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/work-items")
public class WorkItemController {
    private final WorkItemCommandService service;

    public WorkItemController(WorkItemCommandService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public ApiResponse<SaveWorkItemResponse> save(@Valid @RequestBody SaveWorkItemRequest request) {
        return ApiResponse.success(SaveWorkItemResponse.from(service.save(request.toData())));
    }

    @PostMapping("/delete")
    public ApiResponse<DeleteWorkItemsResponse> delete(@Valid @RequestBody DeleteWorkItemsRequest request) {
        return ApiResponse.success(new DeleteWorkItemsResponse(service.delete(request.workItemNos())));
    }
}
