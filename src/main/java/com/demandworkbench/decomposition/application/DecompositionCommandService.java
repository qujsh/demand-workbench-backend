package com.demandworkbench.decomposition.application;

import com.demandworkbench.config.DeepSeekProperties;
import com.demandworkbench.decomposition.controller.response.SubmitResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class DecompositionCommandService {
    private final DecompositionRepository repository;
    private final DecompositionJobRunner jobRunner;
    private final TaskExecutor taskExecutor;
    private final DeepSeekProperties properties;

    public DecompositionCommandService(DecompositionRepository repository,
                                       DecompositionJobRunner jobRunner,
                                       TaskExecutor decompositionExecutor,
                                       DeepSeekProperties properties) {
        this.repository = repository;
        this.jobRunner = jobRunner;
        this.taskExecutor = decompositionExecutor;
        this.properties = properties;
    }

    public SubmitResponse submit(String content) {
        var taskId = "DEC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
        var normalizedContent = content.trim();
        repository.create(taskId, normalizedContent, properties.model(), properties.promptVersion(), LocalDateTime.now());
        try {
            taskExecutor.execute(() -> jobRunner.run(taskId, normalizedContent));
        } catch (RuntimeException exception) {
            repository.fail(taskId, "TASK_QUEUE_FULL", "任务队列繁忙，请稍后重试");
            throw new DecompositionException("TASK_QUEUE_FULL", "任务队列繁忙，请稍后重试", exception);
        }
        return new SubmitResponse(taskId, "QUEUED", "拆解任务已提交");
    }
}
