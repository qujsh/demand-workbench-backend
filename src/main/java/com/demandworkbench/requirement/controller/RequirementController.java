package com.demandworkbench.requirement.controller;

import com.demandworkbench.common.ApiResponse;
import com.demandworkbench.requirement.application.RequirementService;
import com.demandworkbench.requirement.controller.request.SaveRequirementRequest;
import com.demandworkbench.requirement.controller.response.RequirementResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@Validated
@RestController
@RequestMapping("/api/v1/requirements")
public class RequirementController {
    private final RequirementService service;

    public RequirementController(RequirementService service) {
        this.service = service;
    }

    /**
     * 新增或保存需求收集数据。
     */
    @PostMapping("/save")
    public ApiResponse<RequirementResponse> save(
            @RequestParam(required = false) String requirementNo,
            @Valid @RequestBody SaveRequirementRequest request) {
        return ApiResponse.success(RequirementResponse.from(service.save(requirementNo, request.toCommand())));
    }

    /**
     * 按业务编号查询需求详情。
     */
    @GetMapping("/detail")
    public ApiResponse<RequirementResponse> detail(@RequestParam @NotBlank String requirementNo) {
        return ApiResponse.success(RequirementResponse.from(service.find(requirementNo)));
    }

    /**
     * 查询需求列表。
     */
    @GetMapping("/list")
    public ApiResponse<List<RequirementResponse>> list() {
        return ApiResponse.success(service.list().stream().map(RequirementResponse::from).toList());
    }
}
