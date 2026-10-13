package com.demandworkbench.requirement.infrastructure.persistence;

import com.demandworkbench.requirement.application.PublishTaskData;
import com.demandworkbench.requirement.application.PublishTaskRepository;
import com.demandworkbench.requirement.application.RequirementErrorCode;
import com.demandworkbench.requirement.application.RequirementException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MyBatisPublishTaskRepository implements PublishTaskRepository {
    private final PublishTaskMapper mapper;

    public MyBatisPublishTaskRepository(PublishTaskMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PublishTaskData findByRequirementNo(String requirementNo) {
        var record = mapper.findByRequirementNo(requirementNo);
        return record == null ? null : toData(record);
    }

    @Override
    @Transactional
    public PublishTaskData save(PublishTaskData task) {
        var record = mapper.findByRequirementNo(task.requirementNo());
        if (record == null) {
            record = toRecord(task);
            mapper.insert(record);
            if (record.getId() == null) throw new RequirementException(RequirementErrorCode.PUBLISH_TASK_SAVE_FAILED);
            record.setTaskNo("TASK-" + String.format("%06d", record.getId()));
            if (mapper.updateTaskNo(record) != 1) {
                throw new RequirementException(RequirementErrorCode.PUBLISH_TASK_SAVE_FAILED);
            }
        } else {
            copy(task, record);
            if (mapper.update(record) != 1) {
                throw new RequirementException(RequirementErrorCode.PUBLISH_TASK_SAVE_FAILED);
            }
        }
        mapper.deleteWorkItems(record.getId());
        for (int index = 0; index < task.workItemNos().size(); index++) {
            if (mapper.insertWorkItem(record.getId(), task.requirementNo(), task.workItemNos().get(index), index) != 1) {
                throw new RequirementException(RequirementErrorCode.INVALID_PUBLISH_TASK);
            }
        }
        return findByRequirementNo(task.requirementNo());
    }

    private PublishTaskData toData(PublishTaskRecord record) {
        return new PublishTaskData(record.getId(), record.getTaskNo(), record.getRequirementNo(), record.getStatus(),
                record.getTitle(), record.getCategory(), record.getSummary(), record.getSkills(),
                record.getExperience(), record.getRecruitCount(), record.getPriceModel(), record.getBudget(),
                record.getStartDate(), record.getDeadline(), record.getWorkMode(), record.getRecruitMode(),
                record.getVisibility(), mapper.findWorkItemNos(record.getId()), record.getPublishedAt());
    }

    private PublishTaskRecord toRecord(PublishTaskData task) {
        var record = new PublishTaskRecord();
        record.setRequirementNo(task.requirementNo());
        copy(task, record);
        return record;
    }

    private void copy(PublishTaskData task, PublishTaskRecord record) {
        record.setStatus(task.status());
        record.setTitle(task.title());
        record.setCategory(task.category());
        record.setSummary(task.summary());
        record.setSkills(task.skills());
        record.setExperience(task.experience());
        record.setRecruitCount(task.recruitCount());
        record.setPriceModel(task.priceModel());
        record.setBudget(task.budget());
        record.setStartDate(task.startDate());
        record.setDeadline(task.deadline());
        record.setWorkMode(task.workMode());
        record.setRecruitMode(task.recruitMode());
        record.setVisibility(task.visibility());
    }
}
