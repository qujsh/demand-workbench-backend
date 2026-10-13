package com.demandworkbench.requirement.application;

public interface PublishTaskRepository {
    PublishTaskData findByRequirementNo(String requirementNo);

    PublishTaskData save(PublishTaskData task);
}
