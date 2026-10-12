package com.demandworkbench.decomposition.application;

import com.demandworkbench.decomposition.domain.type.TaskStage;
import com.demandworkbench.decomposition.domain.type.TaskStatus;
import org.springframework.stereotype.Service;

@Service
public class DecompositionJobRunner {
    private final WorkItemGenerator generator;
    private final DecompositionRepository repository;

    public DecompositionJobRunner(WorkItemGenerator generator, DecompositionRepository repository) {
        this.generator = generator;
        this.repository = repository;
    }

    /**
     * 执行 AI 生成、结果校验和持久化，并在失败时记录任务错误。
     *
     * @param taskId 拆解任务编号
     * @param content 待拆解的需求内容
     */
    public void run(String taskId, String content) {
        try {
            repository.updateStage(taskId, TaskStatus.PROCESSING, TaskStage.AI_GENERATING);
            var result = generator.generate(content);
            repository.updateStage(taskId, TaskStatus.PROCESSING, TaskStage.VALIDATING);
            validate(result);
            repository.updateStage(taskId, TaskStatus.PROCESSING, TaskStage.SAVING);
            repository.complete(taskId, result);
        } catch (DecompositionException exception) {
            repository.fail(taskId, exception.getErrorCode(), truncate(exception.getMessage()));
        } catch (Exception exception) {
            var errorCode = DecompositionErrorCode.DECOMPOSITION_FAILED;
            repository.fail(taskId, errorCode, errorCode.getMsg());
        }
    }

    private void validate(WorkItemGenerator.GenerationResult result) {
        if (result == null || result.tree() == null || result.tree().modules().isEmpty()) {
            throw new DecompositionException(
                    DecompositionErrorCode.INVALID_MODEL_OUTPUT, "模型没有返回可保存的工作项");
        }
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) return "工作项拆解失败";
        return value.length() <= 500 ? value : value.substring(0, 500);
    }
}
