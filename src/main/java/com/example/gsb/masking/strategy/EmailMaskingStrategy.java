package com.example.gsb.masking.strategy;

import com.example.gsb.masking.MaskingStrategy;

/** 邮箱：本地部分仅保留首字符，例如 {@code z***@example.com}。 */
public class EmailMaskingStrategy implements MaskingStrategy {

    @Override
    public String mask(String raw) {
        if (raw == null) {
            return null;
        }
        int at = raw.indexOf('@');
        if (at <= 0) {
            return MaskingSupport.maskMiddle(raw, 1, 0);
        }
        return raw.charAt(0) + "***" + raw.substring(at);
    }
}
