package com.demandworkbench.requirement.application;

import java.util.LinkedHashSet;
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
        if (requirementRepository.findByRequirementNo(requirementNo) == null) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
        }
        var workItemNos = new LinkedHashSet<>(command.workItemNos());
        if ("published".equals(command.status()) &&
                (command.title().isBlank() || command.summary().isBlank() || workItemNos.isEmpty()
                        || command.recruitCount() < 1)) {
            throw new RequirementException(RequirementErrorCode.INVALID_PUBLISH_TASK);
        }
        if (!"draft".equals(command.status()) && !"published".equals(command.status())) {
            throw new RequirementException(RequirementErrorCode.INVALID_PUBLISH_TASK);
        }
        var saved = taskRepository.save(new PublishTaskData(null, null, requirementNo, command.status(),
                command.title().trim(), command.category(), command.summary().trim(), command.skills(),
                command.experience(), command.recruitCount(), command.priceModel(), command.budget(),
                command.startDate(), command.deadline(), command.workMode(), command.recruitMode(),
                command.visibility(), workItemNos.stream().toList(), null));
        if ("published".equals(saved.status())) {
            requirementRepository.updateStage(requirementNo, "published");
        }
        return saved;
    }
}
