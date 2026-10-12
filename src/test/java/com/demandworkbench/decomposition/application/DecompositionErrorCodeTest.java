package com.demandworkbench.decomposition.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DecompositionErrorCodeTest {
    @Test
    void shouldUseUniqueExternalCodes() {
        var codes = Arrays.stream(DecompositionErrorCode.values())
                .map(DecompositionErrorCode::getCode)
                .toList();

        assertThat(codes).doesNotHaveDuplicates().allMatch(code -> code.matches("S048101\\d{3}"));
    }

    @Test
    void shouldResolveErrorByStableCode() {
        assertThat(DecompositionErrorCode.fromCode("S048101004"))
                .isEqualTo(DecompositionErrorCode.AI_REQUEST_FAILED);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> DecompositionErrorCode.fromCode("UNKNOWN"));
    }
}
