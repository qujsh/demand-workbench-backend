package com.demandworkbench.decomposition.application;

import com.demandworkbench.config.DeepSeekProperties;
import com.demandworkbench.decomposition.domain.type.TaskStatus;
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

    /**
     * 创建需求拆解任务并提交到专用执行器。
     *
     * @param content 已通过接口校验的需求内容
     * @return 新建任务的编号和初始状态
     * @throws DecompositionException 任务无法提交到执行队列时抛出
     */
    public SubmitResult submit(String content) {
        var taskId = "DEC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
        var normalizedContent = content.trim();
        repository.create(taskId, normalizedContent, properties.model(), properties.promptVersion(), LocalDateTime.now());
        try {
            taskExecutor.execute(() -> jobRunner.run(taskId, normalizedContent));
        } catch (RuntimeException exception) {
            var errorCode = DecompositionErrorCode.TASK_QUEUE_FULL;
            repository.fail(taskId, errorCode, errorCode.getMsg());
            throw new DecompositionException(errorCode, errorCode.getMsg(), exception);
        }
        return new SubmitResult(taskId, TaskStatus.QUEUED.getCode(), "拆解任务已提交");
    }
}
