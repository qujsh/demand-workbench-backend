package com.demandworkbench.decomposition.domain.type;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class BusinessEnumTest {
    @Test
    void shouldResolveEnumsByStableCode() {
        assertThat(TaskStatus.fromCode("processing")).isEqualTo(TaskStatus.PROCESSING);
        assertThat(TaskStage.fromCode("ai_generating")).isEqualTo(TaskStage.AI_GENERATING);
        assertThat(WorkItemType.fromCode("subtask")).isEqualTo(WorkItemType.SUBTASK);
        assertThat(WorkItemType.atLevel(2)).isEqualTo(WorkItemType.WORK);
    }

    @Test
    void shouldNormalizePriorityToStableCode() {
        assertThat(Priority.normalize("高")).isEqualTo("high");
        assertThat(Priority.normalize("medium")).isEqualTo("medium");
        assertThat(Priority.normalize("")).isEqualTo("unset");
    }

    @Test
    void shouldRejectUnknownEnumCode() {
        assertThatIllegalArgumentException().isThrownBy(() -> TaskStatus.fromCode("unknown"));
        assertThatIllegalArgumentException().isThrownBy(() -> Priority.normalize("紧急"));
    }
}
