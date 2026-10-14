package com.demandworkbench.requirement.infrastructure.persistence;

import com.demandworkbench.requirement.application.RequirementData;
import com.demandworkbench.requirement.application.RequirementErrorCode;
import com.demandworkbench.requirement.application.RequirementException;
import com.demandworkbench.requirement.application.RequirementRepository;
import com.demandworkbench.requirement.application.PublishSummaryData;
import com.demandworkbench.requirement.domain.service.RequirementNumberGenerator;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MyBatisRequirementRepository implements RequirementRepository {
    private final RequirementMapper requirementMapper;
    private final RequirementAttachmentMapper attachmentMapper;
    private final RequirementNumberGenerator numberGenerator = new RequirementNumberGenerator();

    public MyBatisRequirementRepository(RequirementMapper requirementMapper,
                                        RequirementAttachmentMapper attachmentMapper) {
        this.requirementMapper = requirementMapper;
        this.attachmentMapper = attachmentMapper;
    }

    @Override
    @Transactional
    public RequirementData create(RequirementData requirement) {
        var record = toRecord(requirement);
        requirementMapper.insert(record);
        record.setRequirementNo(numberGenerator.generateRequirementNo(record.getTitleCode(), record.getId()));
        if (requirementMapper.updateRequirementNo(record) != 1) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_SAVE_FAILED);
        }
        replaceAttachments(record.getId(), requirement.attachments());
        return findById(record.getId(), record.getOwnerUserId());
    }

    @Override
    @Transactional
    public RequirementData update(RequirementData requirement) {
        var record = toRecord(requirement);
        if (requirementMapper.update(record) != 1) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
        }
        replaceAttachments(record.getId(), requirement.attachments());
        return findById(record.getId(), record.getOwnerUserId());
    }

    @Override
    public RequirementData findByRequirementNo(long ownerUserId, String requirementNo) {
        var record = requirementMapper.findByRequirementNo(ownerUserId, requirementNo);
        return record == null ? null : toData(record);
    }

    @Override
    public List<RequirementData> findAll(long ownerUserId) {
        return requirementMapper.findAll(ownerUserId).stream().map(this::toData).toList();
    }

    @Override
    public PublishSummaryData findPublishSummary(long ownerUserId, String requirementNo) {
        var record = requirementMapper.findPublishSummary(ownerUserId, requirementNo);
        return record == null ? null : new PublishSummaryData(record.getRequirementNo(),
                record.getPublishSummaryStatus(), record.getPublishSummary(),
                record.getPublishSummaryPromptVersion(), record.getPublishSummaryGeneratedAt());
    }

    @Override
    public void updatePublishSummaryStatus(long ownerUserId, String requirementNo, String status, String promptVersion) {
        if (requirementMapper.updatePublishSummaryStatus(ownerUserId, requirementNo, status, promptVersion) != 1) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
        }
    }

    @Override
    public void savePublishSummary(long ownerUserId, String requirementNo, String content, String promptVersion) {
        if (requirementMapper.savePublishSummary(ownerUserId, requirementNo, content, promptVersion) != 1) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
        }
    }

    @Override
    public void updateStage(long ownerUserId, String requirementNo, String stage) {
        if (requirementMapper.updateStage(ownerUserId, requirementNo, stage) != 1) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
        }
    }

    @Override
    public boolean softDelete(long ownerUserId, String requirementNo) {
        return requirementMapper.softDelete(ownerUserId, requirementNo) == 1;
    }

    private RequirementData findById(long id, long ownerUserId) {
        var record = requirementMapper.findById(id, ownerUserId);
        if (record == null) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
        }
        return toData(record);
    }

    private void replaceAttachments(long requirementId, List<RequirementData.AttachmentData> attachments) {
        attachmentMapper.softDeleteByRequirementId(requirementId);
        for (var attachment : attachments) {
            var record = new RequirementAttachmentRecord();
            record.setRequirementId(requirementId);
            record.setFileName(attachment.fileName());
            record.setStorageKey(attachment.storageKey());
            record.setContentType(attachment.contentType());
            record.setFileSize(attachment.fileSize());
            record.setSortOrder(attachment.sortOrder());
            attachmentMapper.insert(record);
        }
    }

    private RequirementRecord toRecord(RequirementData data) {
        var record = new RequirementRecord();
        record.setId(data.id());
        record.setOwnerUserId(data.ownerUserId());
        record.setRequirementNo(data.requirementNo());
        record.setTitleCode(data.titleCode());
        record.setTitle(data.title());
        record.setUserDescription(data.userDescription());
        record.setAiDescription(data.aiDescription());
        record.setStage(data.stage());
        record.setCurrentDecompositionNo(data.currentDecompositionNo());
        return record;
    }

    private RequirementData toData(RequirementRecord record) {
        var attachments = attachmentMapper.findByRequirementId(record.getId()).stream()
                .map(item -> new RequirementData.AttachmentData(item.getId(), item.getFileName(),
                        item.getStorageKey(), item.getContentType(), item.getFileSize(), item.getSortOrder()))
                .toList();
        return new RequirementData(record.getId(), record.getOwnerUserId(), record.getRequirementNo(), record.getTitleCode(),
                record.getTitle(), record.getUserDescription(), record.getAiDescription(), record.getStage(),
                record.getCurrentDecompositionNo(), record.getCreatedAt(), record.getUpdatedAt(),
                record.getModuleCount(), record.getWorkItemCount(), attachments);
    }
}
