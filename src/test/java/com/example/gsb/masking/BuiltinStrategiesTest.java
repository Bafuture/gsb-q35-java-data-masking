package com.example.gsb.masking;

import com.example.gsb.masking.strategy.AddressMaskingStrategy;
import com.example.gsb.masking.strategy.BankCardMaskingStrategy;
import com.example.gsb.masking.strategy.EmailMaskingStrategy;
import com.example.gsb.masking.strategy.IdCardMaskingStrategy;
import com.example.gsb.masking.strategy.NameMaskingStrategy;
import com.example.gsb.masking.strategy.PhoneMaskingStrategy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuiltinStrategiesTest {

    @Test
    void phoneKeepsHead3AndTail4() {
        assertThat(new PhoneMaskingStrategy().mask("13812345678")).isEqualTo("138****5678");
        assertThat(new PhoneMaskingStrategy().mask("12345")).isEqualTo("*****");
    }

    @Test
    void idCardKeepsHead6AndTail4() {
        assertThat(new IdCardMaskingStrategy().mask("110101199001011234"))
                .isEqualTo("110101********1234");
    }

    @Test
    void bankCardKeepsOnlyTail4() {
        assertThat(new BankCardMaskingStrategy().mask("6222021234567891234"))
                .isEqualTo("***************1234");
    }

    @Test
    void emailKeepsFirstCharOfLocalPart() {
        assertThat(new EmailMaskingStrategy().mask("zhangsan@example.com"))
                .isEqualTo("z***@example.com");
        assertThat(new EmailMaskingStrategy().mask("no-at-sign")).isEqualTo("n*********");
    }

    @Test
    void nameKeepsFirstChar() {
        assertThat(new NameMaskingStrategy().mask("欧阳娜娜")).isEqualTo("欧***");
        assertThat(new NameMaskingStrategy().mask("张三")).isEqualTo("张*");
        assertThat(new NameMaskingStrategy().mask("张")).isEqualTo("*");
    }

    @Test
    void addressKeepsDistrictLevelPrefix() {
        assertThat(new AddressMaskingStrategy().mask("北京市海淀区中关村大街27号"))
                .isEqualTo("北京市海淀区****");
        assertThat(new AddressMaskingStrategy().mask("北京市")).isEqualTo("****");
    }
}
