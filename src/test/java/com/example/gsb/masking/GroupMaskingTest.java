package com.example.gsb.masking;

import com.example.gsb.masking.strategy.IdCardMaskingStrategy;
import com.example.gsb.masking.strategy.NameMaskingStrategy;
import com.example.gsb.masking.strategy.PhoneMaskingStrategy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroupMaskingTest {

    static class Account {
        @Sensitive(strategy = PhoneMaskingStrategy.class, groups = "log")
        @Sensitive(strategy = IdCardMaskingStrategy.class, groups = "audit")
        String phone = "13812345678";

        @Sensitive(strategy = NameMaskingStrategy.class)
        String owner = "张三丰";

        @SensitiveIgnore
        String traceId = "trace-13812345678";
    }

    @Test
    void logGroupUsesLogStrategy() {
        MaskingEngine engine = new MaskingEngine(
                MaskingConfig.builder().activeGroups("log").build());
        Account masked = engine.mask(new Account());

        assertThat(masked.phone).isEqualTo("138****5678");
        assertThat(masked.owner).isEqualTo("张**");
    }

    @Test
    void auditGroupUsesAuditStrategy() {
        MaskingEngine engine = new MaskingEngine(
                MaskingConfig.builder().activeGroups("audit").build());
        Account masked = engine.mask(new Account());

        assertThat(masked.phone).isEqualTo("138123*5678");
    }

    @Test
    void noActiveGroupLeavesGroupedFieldUntouched() {
        MaskingEngine engine = new MaskingEngine();
        Account masked = engine.mask(new Account());

        assertThat(masked.phone).isEqualTo("13812345678");
        assertThat(masked.owner).isEqualTo("张**");
    }

    @Test
    void sensitiveIgnoreExcludesFieldFromMasking() {
        MaskingEngine engine = new MaskingEngine(
                MaskingConfig.builder().activeGroups("log").build());
        Account masked = engine.mask(new Account());

        assertThat(masked.traceId).isEqualTo("trace-13812345678");
    }
}
