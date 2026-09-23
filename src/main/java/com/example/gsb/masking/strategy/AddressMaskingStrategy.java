package com.example.gsb.masking.strategy;

import com.example.gsb.masking.MaskingStrategy;

/** 地址：保留大致到区县级（前 6 字），其余以 {@code ****} 代替。 */
public class AddressMaskingStrategy implements MaskingStrategy {

    @Override
    public String mask(String raw) {
        if (raw == null) {
            return null;
        }
        int len = raw.length();
        int keep = len > 9 ? 6 : (len > 3 ? 3 : 0);
        return raw.substring(0, keep) + "****";
    }
}
