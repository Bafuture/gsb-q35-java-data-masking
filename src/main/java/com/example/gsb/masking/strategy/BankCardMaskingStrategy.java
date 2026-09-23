package com.example.gsb.masking.strategy;

import com.example.gsb.masking.MaskingStrategy;

/** 银行卡号：仅保留后 4 位，例如 {@code ***************1234}。 */
public class BankCardMaskingStrategy implements MaskingStrategy {

    @Override
    public String mask(String raw) {
        return MaskingSupport.maskMiddle(raw, 0, 4);
    }
}
