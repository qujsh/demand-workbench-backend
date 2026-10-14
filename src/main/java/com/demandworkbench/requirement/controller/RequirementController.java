package com.demandworkbench.requirement.controller;

import com.demandworkbench.common.ApiResponse;
import com.demandworkbench.requirement.application.PublishSummaryService;
import com.demandworkbench.requirement.application.PublishTaskService;
import com.demandworkbench.requirement.application.RequirementService;
import com.demandworkbench.requirement.controller.request.SaveRequirementRequest;
import com.demandworkbench.requirement.controller.request.SavePublishTaskRequest;
import com.demandworkbench.requirement.controller.response.PublishSummaryResponse;
import com.demandworkbench.requirement.controller.response.PublishTaskResponse;
import com.demandworkbench.requirement.controller.response.RequirementResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import com.demandworkbench.identity.application.AuthenticatedUser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Validated
@RestController
@RequestMapping("/api/v1/requirements")
public class RequirementController {
    private final RequirementService service;
    private final PublishSummaryService publishSummaryService;
    private final PublishTaskService publishTaskService;

    public RequirementController(RequirementService service, PublishSummaryService publishSummaryService,
                                 PublishTaskService publishTaskService) {
        this.service = service;
        this.publishSummaryService = publishSummaryService;
        this.publishTaskService = publishTaskService;
    }

    /**
     * 新增或保存需求收集数据。
     */
    @PostMapping("/save")
    public ApiResponse<RequirementResponse> save(
            @RequestParam(required = false) String requirementNo,
            @Valid @RequestBody SaveRequirementRequest request, Authentication authentication) {
        return ApiResponse.success(RequirementResponse.from(service.save(currentUserId(authentication), requirementNo, request.toCommand())));
    }

    /**
     * 按业务编号查询需求详情。
     */
    @GetMapping("/detail")
    public ApiResponse<RequirementResponse> detail(@RequestParam @NotBlank String requirementNo, Authentication authentication) {
        return ApiResponse.success(RequirementResponse.from(service.find(currentUserId(authentication), requirementNo)));
    }

    /**
     * 查询需求列表。
     */
    @GetMapping("/list")
    public ApiResponse<List<RequirementResponse>> list(Authentication authentication) {
        return ApiResponse.success(service.list(currentUserId(authentication)).stream().map(RequirementResponse::from).toList());
    }

    /** 查询需求对应的任务发布草稿或正式任务。 */
    @GetMapping("/publish-task")
    public ApiResponse<PublishTaskResponse> publishTask(@RequestParam @NotBlank String requirementNo, Authentication authentication) {
        return ApiResponse.success(PublishTaskResponse.from(publishTaskService.find(currentUserId(authentication), requirementNo)));
    }

    /** 保存发布草稿或确认发布任务。 */
    @PostMapping("/publish-task/save")
    public ApiResponse<PublishTaskResponse> savePublishTask(
            @RequestParam @NotBlank String requirementNo,
            @Valid @RequestBody SavePublishTaskRequest request, Authentication authentication) {
        return ApiResponse.success(PublishTaskResponse.from(
                publishTaskService.save(currentUserId(authentication), requirementNo, request.toData(requirementNo))));
    }

    /** 取消已经发布的任务。 */
    @PostMapping("/publish-task/cancel")
    public ApiResponse<PublishTaskResponse> cancelPublishTask(
            @RequestParam @NotBlank String requirementNo, Authentication authentication) {
        return ApiResponse.success(PublishTaskResponse.from(publishTaskService.cancel(currentUserId(authentication), requirementNo)));
    }

    /**
     * 查询需求对应的发布任务简介及生成状态。
     */
    @GetMapping("/publish-summary")
    public ApiResponse<PublishSummaryResponse> publishSummary(
            @RequestParam @NotBlank String requirementNo, Authentication authentication) {
        return ApiResponse.success(PublishSummaryResponse.from(publishSummaryService.find(currentUserId(authentication), requirementNo)));
    }

    /**
     * 流式生成需求对应的发布任务简介。
     */
    @PostMapping(value = "/publish-summary/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generatePublishSummary(@RequestParam @NotBlank String requirementNo, Authentication authentication) {
        var userId = currentUserId(authentication);
        var emitter = new SseEmitter(300_000L);
        var clientConnected = new AtomicBoolean(true);
        emitter.onCompletion(() -> clientConnected.set(false));
        emitter.onTimeout(() -> clientConnected.set(false));
        publishSummaryService.generate(userId, requirementNo).subscribe(
                chunk -> sendEvent(emitter, clientConnected, "content", new ContentEvent(chunk)),
                error -> {
                    sendEvent(emitter, clientConnected, "error",
                            new ErrorEvent("S048102006", "任务简介生成失败"));
                    emitter.complete();
                },
                () -> {
                    var completed = publishSummaryService.find(userId, requirementNo);
                    sendEvent(emitter, clientConnected, "complete", PublishSummaryResponse.from(completed));
                    emitter.complete();
                });
        return emitter;
    }

    /**
     * 按业务编号软删除需求。
     */
    @PostMapping("/delete")
    public ApiResponse<String> delete(@RequestParam @NotBlank String requirementNo, Authentication authentication) {
        service.delete(currentUserId(authentication), requirementNo);
        return ApiResponse.success("deleted");
    }

    private void sendEvent(SseEmitter emitter, AtomicBoolean clientConnected, String name, Object data) {
        if (!clientConnected.get()) return;
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException exception) {
            clientConnected.set(false);
        }
    }

    private long currentUserId(Authentication authentication) {
        return authentication == null ? 1L : ((AuthenticatedUser) authentication.getPrincipal()).id();
    }

    private record ContentEvent(String text) {
    }

    private record ErrorEvent(String errorCode, String message) {
    }
}
