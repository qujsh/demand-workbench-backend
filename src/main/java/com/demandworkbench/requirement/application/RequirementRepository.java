package com.demandworkbench.requirement.application;

import java.util.List;

public interface RequirementRepository {
    RequirementData create(RequirementData requirement);

    RequirementData update(RequirementData requirement);

    RequirementData findByRequirementNo(String requirementNo);

    List<RequirementData> findAll();

    boolean softDelete(String requirementNo);
}
