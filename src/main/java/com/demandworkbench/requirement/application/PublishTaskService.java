package com.demandworkbench.requirement.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublishTaskService {
    private final PublishTaskRepository taskRepository;
    private final RequirementRepository requirementRepository;

    public PublishTaskService(PublishTaskRepository taskRepository, RequirementRepository requirementRepository) {
        this.taskRepository = taskRepository;
        this.requirementRepository = requirementRepository;
    }

    public PublishTaskData find(String requirementNo) {
        if (requirementRepository.findByRequirementNo(requirementNo) == null) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
        }
        return taskRepository.findByRequirementNo(requirementNo);
    }

    @Transactional
    public PublishTaskData save(String requirementNo, PublishTaskData command) {
        var requirement = requirementRepository.findByRequirementNo(requirementNo);
        if (requirement == null) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
        }
        if ("published".equals(command.status()) &&
                (command.title().isBlank() || command.summary().isBlank()
                        || requirement.currentDecompositionNo() == null
                        || command.recruitCount() < 1)) {
            throw new RequirementException(RequirementErrorCode.INVALID_PUBLISH_TASK);
        }
        if (!"draft".equals(command.status()) && !"published".equals(command.status())) {
            throw new RequirementException(RequirementErrorCode.INVALID_PUBLISH_TASK);
        }
        var existingTask = taskRepository.findByRequirementNo(requirementNo);
        var savedStatus = existingTask != null && "cancelled".equals(existingTask.status())
                && "draft".equals(command.status()) ? "cancelled" : command.status();
        var saved = taskRepository.save(new PublishTaskData(null, null, requirementNo,
                requirement.currentDecompositionNo(), savedStatus,
                command.title().trim(), command.category(), command.summary().trim(), command.skills(),
                command.experience(), command.recruitCount(), command.priceModel(), command.budget(),
                command.startDate(), command.deadline(), command.workMode(), command.recruitMode(),
                command.visibility(), null));
        if ("published".equals(saved.status())) {
            requirementRepository.updateStage(requirementNo, "published");
        }
        return saved;
    }

    @Transactional
    public PublishTaskData cancel(String requirementNo) {
        var task = taskRepository.findByRequirementNo(requirementNo);
        if (task == null || !"published".equals(task.status())) {
            throw new RequirementException(RequirementErrorCode.PUBLISH_TASK_CANCEL_FAILED);
        }
        var cancelled = taskRepository.cancel(requirementNo);
        requirementRepository.updateStage(requirementNo, "cancelled");
        return cancelled;
    }
}
