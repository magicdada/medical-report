package com.medical.common.enums;

/**
 * 疾病类型枚举
 *
 * @author wangda
 * @since 2026/08/11
 */
public enum DiseaseEnum {

    /**
     * 疾病类型及对应的报告关键词
     */
    NORMAL("Normal", new String[]{"clear", "normal"}),
    CARDIOMEGALY("Cardiomegaly", new String[]{"cardiomegaly"}),
    PNEUMONIA("Pneumonia", new String[]{"pneumonia"}),
    EFFUSION("Effusion", new String[]{"effusion"}),
    ATELECTASIS("Atelectasis", new String[]{"atelectasis"});

    private final String name;
    private final String[] keywords;

    DiseaseEnum(String name, String[] keywords) {
        this.name = name;
        this.keywords = keywords;
    }

    public String getName() {
        return this.name;
    }

    public String[] getKeywords() {
        return this.keywords;
    }

    /**
     * 判断报告内容是否包含该疾病关键词
     *
     * @param content 报告内容（小写）
     * @return 是否匹配
     */
    public boolean matches(String content) {
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
