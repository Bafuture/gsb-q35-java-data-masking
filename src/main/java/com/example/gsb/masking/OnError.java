package com.example.gsb.masking;

/** 脱敏过程中发生异常时的兜底策略。 */
public enum OnError {

    /** 保留原始值（不脱敏，但流程继续）。 */
    KEEP_ORIGINAL,

    /** 替换为占位符（字符串字段用占位符，其他类型字段置 null）。 */
    PLACEHOLDER
}
