package com.demandworkbench.requirement.domain.service;

import net.sourceforge.pinyin4j.PinyinHelper;

public final class RequirementNumberGenerator {
    private static final int MAX_TITLE_CODE_LENGTH = 6;
    private static final String FALLBACK_TITLE_CODE = "COMMON";

    /**
     * 根据标题生成用于需求编号的稳定可读编码。
     */
    public String generateTitleCode(String title) {
        var code = new StringBuilder(MAX_TITLE_CODE_LENGTH);
        for (var index = 0; index < title.length() && code.length() < MAX_TITLE_CODE_LENGTH; index++) {
            var character = title.charAt(index);
            if (isChinese(character)) {
                var pinyin = PinyinHelper.toHanyuPinyinStringArray(character);
                if (pinyin != null && pinyin.length > 0 && !pinyin[0].isEmpty()) {
                    code.append(Character.toUpperCase(pinyin[0].charAt(0)));
                }
            } else if (Character.isLetterOrDigit(character)) {
                code.append(Character.toUpperCase(character));
            }
        }
        return code.isEmpty() ? FALLBACK_TITLE_CODE : code.toString();
    }

    /**
     * 使用标题编码和数据库主键生成对外需求编号。
     */
    public String generateRequirementNo(String titleCode, long id) {
        return "REQ-%s-%06d".formatted(titleCode, id);
    }

    private boolean isChinese(char character) {
        var block = Character.UnicodeBlock.of(character);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A;
    }
}
