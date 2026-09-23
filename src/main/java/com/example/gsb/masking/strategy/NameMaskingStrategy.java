package com.example.gsb.masking.strategy;

import com.example.gsb.masking.MaskingStrategy;

/** 姓名：保留首字，其余替换为 {@code *}，例如 {@code 欧***}。 */
public class NameMaskingStrategy implements MaskingStrategy {

    @Override
    public String mask(String raw) {
        if (raw == null) {
            return null;
        }
        if (raw.length() <= 1) {
            return "*";
        }
        return raw.charAt(0) + "*".repeat(raw.length() - 1);
    }
}
