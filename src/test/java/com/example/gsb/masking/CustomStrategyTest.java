package com.example.gsb.masking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomStrategyTest {

    public static class HashTagStrategy implements MaskingStrategy {
        @Override
        public String mask(String raw) {
            return "[hash:" + Integer.toHexString(raw.hashCode()) + "]";
        }
    }

    static class Order {
        @Sensitive(strategy = HashTagStrategy.class)
        String coupon = "SUMMER2026";
    }

    @Test
    void customStrategyIsApplied() {
        MaskingEngine engine = new MaskingEngine();

        Order masked = engine.mask(new Order());

        assertThat(masked.coupon).isEqualTo("[hash:" + Integer.toHexString("SUMMER2026".hashCode()) + "]");
    }
}
