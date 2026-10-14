package com.demandworkbench.decomposition.controller;

import com.demandworkbench.common.ApiResponse;
import com.demandworkbench.decomposition.application.DecompositionCommandService;
import com.demandworkbench.decomposition.application.DecompositionQueryService;
import com.demandworkbench.decomposition.controller.request.DecomposeRequest;
import com.demandworkbench.decomposition.controller.response.SubmitResponse;
import com.demandworkbench.decomposition.controller.response.TaskResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.demandworkbench.identity.application.AuthenticatedUser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/requirements/decompose")
public class DecompositionController {
    private final DecompositionCommandService commandService;
    private final DecompositionQueryService queryService;

    public DecompositionController(DecompositionCommandService commandService,
                                   DecompositionQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    /**
     * 接收需求内容并创建异步拆解任务。
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SubmitResponse>> submit(@Valid @RequestBody DecomposeRequest request,
                                                               Authentication authentication) {
        var userId = currentUserId(authentication);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(SubmitResponse.from(commandService.submit(userId, request.content()))));
    }

    /**
     * 查询拆解任务的当前状态和结果。
     */
    @GetMapping
    public ApiResponse<TaskResponse> query(@RequestParam @NotBlank String taskId, Authentication authentication) {
        return ApiResponse.success(TaskResponse.from(queryService.query(currentUserId(authentication), taskId)));
    }

    private long currentUserId(Authentication authentication) {
        return authentication == null ? 1L : ((AuthenticatedUser) authentication.getPrincipal()).id();
    }
}
