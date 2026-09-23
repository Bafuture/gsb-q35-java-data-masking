package com.example.gsb.masking;

import com.example.gsb.masking.log.LogMaskingHook;
import com.example.gsb.masking.log.RegexLogMaskingHook;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogMaskingHookTest {

    private final LogMaskingHook hook = new RegexLogMaskingHook();

    @Test
    void masksPhoneIdCardBankCardAndEmailInLogMessage() {
        String message = "用户张三 手机13812345678 身份证110101199001011234 "
                + "卡号6222021234567891234 邮箱zhangsan@example.com 登录失败";

        String masked = hook.mask(message);

        assertThat(masked).contains("138****5678");
        assertThat(masked).contains("110101********1234");
        assertThat(masked).contains("***************1234");
        assertThat(masked).contains("z***@example.com");
        assertThat(masked).doesNotContain("13812345678");
        assertThat(masked).doesNotContain("110101199001011234");
        assertThat(masked).doesNotContain("6222021234567891234");
        assertThat(masked).doesNotContain("zhangsan@example.com");
    }

    @Test
    void leavesOrdinaryTextUntouched() {
        assertThat(hook.mask("order id 12345 created")).isEqualTo("order id 12345 created");
        assertThat(hook.mask("")).isEmpty();
        assertThat(hook.mask(null)).isEmpty();
    }
}
