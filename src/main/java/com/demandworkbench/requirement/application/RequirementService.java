package com.demandworkbench.requirement.application;

import com.demandworkbench.decomposition.application.DecompositionRepository;
import com.demandworkbench.decomposition.domain.type.TaskStatus;
import com.demandworkbench.requirement.domain.service.RequirementNumberGenerator;
import com.demandworkbench.requirement.domain.type.RequirementStage;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RequirementService {
    private final RequirementRepository repository;
    private final DecompositionRepository decompositionRepository;
    private final RequirementNumberGenerator numberGenerator = new RequirementNumberGenerator();

    public RequirementService(RequirementRepository repository, DecompositionRepository decompositionRepository) {
        this.repository = repository;
        this.decompositionRepository = decompositionRepository;
    }

    /**
     * 新增或保存需求收集数据；需求编号仅在首次创建时生成。
     */
    public RequirementData save(long ownerUserId, String requirementNo, SaveCommand command) {
        var stage = parseStage(command.stage());
        validateDecomposition(ownerUserId, command.currentDecompositionNo());
        if (requirementNo == null || requirementNo.isBlank()) {
            var titleCode = numberGenerator.generateTitleCode(command.title());
            return repository.create(toData(ownerUserId, null, null, titleCode, stage, command));
        }
        var existing = find(ownerUserId, requirementNo);
        if (RequirementStage.PUBLISHED.getCode().equals(existing.stage())
                || RequirementStage.CANCELLED.getCode().equals(existing.stage())) {
            stage = existing.stage();
        }
        return repository.update(toData(ownerUserId, existing.id(), existing.requirementNo(), existing.titleCode(), stage, command));
    }

    public RequirementData save(String requirementNo, SaveCommand command) { return save(1L, requirementNo, command); }

    private String parseStage(String stage) {
        try {
            return RequirementStage.fromCode(stage).getCode();
        } catch (IllegalArgumentException exception) {
            throw new RequirementException(RequirementErrorCode.INVALID_REQUIREMENT);
        }
    }

    /**
     * 按对外需求编号查询需求详情。
     */
    public RequirementData find(long ownerUserId, String requirementNo) {
        var requirement = repository.findByRequirementNo(ownerUserId, requirementNo);
        if (requirement == null) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
        }
        return requirement;
    }

    public RequirementData find(String requirementNo) { return find(1L, requirementNo); }

    /**
     * 查询需求列表，按最后更新时间倒序返回。
     */
    public List<RequirementData> list(long ownerUserId) {
        return repository.findAll(ownerUserId);
    }

    public List<RequirementData> list() { return list(1L); }

    /**
     * 按业务编号软删除需求。
     */
    public void delete(long ownerUserId, String requirementNo) {
        if (!repository.softDelete(ownerUserId, requirementNo)) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
        }
    }

    public void delete(String requirementNo) { delete(1L, requirementNo); }

    private RequirementData toData(long ownerUserId, Long id, String requirementNo, String titleCode, String stage,
                                   SaveCommand command) {
        return new RequirementData(id, ownerUserId, requirementNo, titleCode, command.title(), command.userDescription(),
                command.aiDescription(), stage, command.currentDecompositionNo(), null, null,
                0, 0, command.attachments());
    }

    private void validateDecomposition(long ownerUserId, String decompositionId) {
        if (decompositionId == null || decompositionId.isBlank()) {
            return;
        }
        var task = decompositionRepository.find(decompositionId);
        if (task == null || task.ownerUserId() != ownerUserId) {
            throw new RequirementException(RequirementErrorCode.DECOMPOSITION_NOT_FOUND);
        }
        if (task.status() != TaskStatus.SUCCESS) {
            throw new RequirementException(RequirementErrorCode.DECOMPOSITION_NOT_COMPLETED);
        }
    }

    public record SaveCommand(
            String title,
            String userDescription,
            String aiDescription,
            String stage,
            String currentDecompositionNo,
            List<RequirementData.AttachmentData> attachments) {
    }
}
