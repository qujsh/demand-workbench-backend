package com.demandworkbench.requirement.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RequirementNumberGeneratorTest {
    private final RequirementNumberGenerator generator = new RequirementNumberGenerator();

    @Test
    void shouldGenerateReadableCodeFromChineseEnglishAndDigits() {
        assertThat(generator.generateTitleCode("开发一套预约服务系统")).isEqualTo("KFYTYY");
        assertThat(generator.generateTitleCode("CRM 2.0 系统")).isEqualTo("CRM20X");
    }

    @Test
    void shouldUseFallbackWhenTitleHasNoSupportedCharacter() {
        assertThat(generator.generateTitleCode("---")).isEqualTo("COMMON");
    }

    @Test
    void shouldCombineTitleCodeAndDatabaseId() {
        assertThat(generator.generateRequirementNo("KFYTYY", 25)).isEqualTo("REQ-KFYTYY-000025");
    }
}
