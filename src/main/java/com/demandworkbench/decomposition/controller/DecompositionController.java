package com.demandworkbench.decomposition.controller;

import com.demandworkbench.decomposition.application.DecompositionCommandService;
import com.demandworkbench.decomposition.application.DecompositionQueryService;
import com.demandworkbench.decomposition.controller.request.DecomposeRequest;
import com.demandworkbench.decomposition.controller.response.SubmitResponse;
import com.demandworkbench.decomposition.controller.response.TaskResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<SubmitResponse> submit(@Valid @RequestBody DecomposeRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(commandService.submit(request.content()));
    }

    @GetMapping
    public TaskResponse query(@RequestParam @NotBlank String taskId) {
        return queryService.query(taskId);
    }
}
