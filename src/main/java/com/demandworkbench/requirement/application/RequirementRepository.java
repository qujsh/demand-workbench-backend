package com.demandworkbench.requirement.application;

import java.util.List;

public interface RequirementRepository {
    RequirementData create(RequirementData requirement);

    RequirementData update(RequirementData requirement);

    RequirementData findByRequirementNo(String requirementNo);

    List<RequirementData> findAll();

    PublishSummaryData findPublishSummary(String requirementNo);

    void updatePublishSummaryStatus(String requirementNo, String status, String promptVersion);

    void savePublishSummary(String requirementNo, String content, String promptVersion);

    void updateStage(String requirementNo, String stage);

    boolean softDelete(String requirementNo);
}
