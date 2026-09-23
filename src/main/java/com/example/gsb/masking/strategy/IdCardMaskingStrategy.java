package com.example.gsb.masking.strategy;

import com.example.gsb.masking.MaskingStrategy;

/** 身份证号：保留前 6 位（地区码）与后 4 位，例如 {@code 110101********1234}。 */
public class IdCardMaskingStrategy implements MaskingStrategy {

    @Override
    public String mask(String raw) {
        return MaskingSupport.maskMiddle(raw, 6, 4);
    }
}
