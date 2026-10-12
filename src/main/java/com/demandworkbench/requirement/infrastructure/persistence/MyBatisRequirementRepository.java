package com.demandworkbench.requirement.infrastructure.persistence;

import com.demandworkbench.requirement.application.RequirementData;
import com.demandworkbench.requirement.application.RequirementErrorCode;
import com.demandworkbench.requirement.application.RequirementException;
import com.demandworkbench.requirement.application.RequirementRepository;
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
        return findById(record.getId());
    }

    @Override
    @Transactional
    public RequirementData update(RequirementData requirement) {
        var record = toRecord(requirement);
        if (requirementMapper.update(record) != 1) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
        }
        replaceAttachments(record.getId(), requirement.attachments());
        return findById(record.getId());
    }

    @Override
    public RequirementData findByRequirementNo(String requirementNo) {
        var record = requirementMapper.findByRequirementNo(requirementNo);
        return record == null ? null : toData(record);
    }

    @Override
    public List<RequirementData> findAll() {
        return requirementMapper.findAll().stream().map(this::toData).toList();
    }

    private RequirementData findById(long id) {
        var record = requirementMapper.findById(id);
        if (record == null) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
        }
        return toData(record);
    }

    private void replaceAttachments(long requirementId, List<RequirementData.AttachmentData> attachments) {
        attachmentMapper.deleteByRequirementId(requirementId);
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
        record.setRequirementNo(data.requirementNo());
        record.setTitleCode(data.titleCode());
        record.setTitle(data.title());
        record.setUserDescription(data.userDescription());
        record.setAiDescription(data.aiDescription());
        record.setStage(data.stage());
        record.setCurrentDecompositionId(data.currentDecompositionId());
        return record;
    }

    private RequirementData toData(RequirementRecord record) {
        var attachments = attachmentMapper.findByRequirementId(record.getId()).stream()
                .map(item -> new RequirementData.AttachmentData(item.getId(), item.getFileName(),
                        item.getStorageKey(), item.getContentType(), item.getFileSize(), item.getSortOrder()))
                .toList();
        return new RequirementData(record.getId(), record.getRequirementNo(), record.getTitleCode(),
                record.getTitle(), record.getUserDescription(), record.getAiDescription(), record.getStage(),
                record.getCurrentDecompositionId(), record.getCreatedAt(), record.getUpdatedAt(), attachments);
    }
}
