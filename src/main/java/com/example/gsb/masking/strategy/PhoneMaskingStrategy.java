package com.example.gsb.masking.strategy;

import com.example.gsb.masking.MaskingStrategy;

/** 手机号：保留前 3 位与后 4 位，例如 {@code 138****5678}。 */
public class PhoneMaskingStrategy implements MaskingStrategy {

    @Override
    public String mask(String raw) {
        return MaskingSupport.maskMiddle(raw, 3, 4);
    }
}
