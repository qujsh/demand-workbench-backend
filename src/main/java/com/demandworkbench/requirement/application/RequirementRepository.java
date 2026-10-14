package com.demandworkbench.requirement.application;

import java.util.List;

public interface RequirementRepository {
    RequirementData create(RequirementData requirement);

    RequirementData update(RequirementData requirement);

    RequirementData findByRequirementNo(long ownerUserId, String requirementNo);

    List<RequirementData> findAll(long ownerUserId);

    PublishSummaryData findPublishSummary(long ownerUserId, String requirementNo);

    void updatePublishSummaryStatus(long ownerUserId, String requirementNo, String status, String promptVersion);

    void savePublishSummary(long ownerUserId, String requirementNo, String content, String promptVersion);

    void updateStage(long ownerUserId, String requirementNo, String stage);

    boolean softDelete(long ownerUserId, String requirementNo);

    default RequirementData findByRequirementNo(String requirementNo) { return findByRequirementNo(1L, requirementNo); }
    default List<RequirementData> findAll() { return findAll(1L); }
    default PublishSummaryData findPublishSummary(String requirementNo) { return findPublishSummary(1L, requirementNo); }
    default void updatePublishSummaryStatus(String requirementNo, String status, String promptVersion) { updatePublishSummaryStatus(1L, requirementNo, status, promptVersion); }
    default void savePublishSummary(String requirementNo, String content, String promptVersion) { savePublishSummary(1L, requirementNo, content, promptVersion); }
    default void updateStage(String requirementNo, String stage) { updateStage(1L, requirementNo, stage); }
    default boolean softDelete(String requirementNo) { return softDelete(1L, requirementNo); }
}
