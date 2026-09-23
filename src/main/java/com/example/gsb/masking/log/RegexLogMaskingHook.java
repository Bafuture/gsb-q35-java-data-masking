package com.example.gsb.masking.log;

import com.example.gsb.masking.MaskingStrategy;
import com.example.gsb.masking.strategy.BankCardMaskingStrategy;
import com.example.gsb.masking.strategy.EmailMaskingStrategy;
import com.example.gsb.masking.strategy.IdCardMaskingStrategy;
import com.example.gsb.masking.strategy.PhoneMaskingStrategy;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于正则的日志消息脱敏钩子：直接在已格式化的日志文本中识别并脱敏
 * 手机号、身份证号、银行卡号、邮箱。无需注解，适合兜底扫描日志输出。
 */
public class RegexLogMaskingHook implements LogMaskingHook {

    private static final Pattern EMAIL =
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern ID_CARD =
            Pattern.compile("(?<!\\d)(\\d{17}[\\dXx]|\\d{15})(?!\\d)");
    private static final Pattern PHONE =
            Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)");
    private static final Pattern BANK_CARD =
            Pattern.compile("(?<!\\d)\\d{16,19}(?!\\d)");

    private final List<Rule> rules;

    public RegexLogMaskingHook() {
        this(new PhoneMaskingStrategy(), new IdCardMaskingStrategy(),
                new BankCardMaskingStrategy(), new EmailMaskingStrategy());
    }

    public RegexLogMaskingHook(MaskingStrategy phone, MaskingStrategy idCard,
                               MaskingStrategy bankCard, MaskingStrategy email) {
        // 顺序敏感：先身份证（18 位含 X），再银行卡，避免互相误匹配。
        this.rules = List.of(
                new Rule(EMAIL, email),
                new Rule(ID_CARD, idCard),
                new Rule(BANK_CARD, bankCard),
                new Rule(PHONE, phone));
    }

    @Override
    public String mask(String message) {
        if (message == null || message.isEmpty()) {
            return message == null ? "" : message;
        }
        String result = message;
        for (Rule rule : rules) {
            Matcher matcher = rule.pattern.matcher(result);
            result = matcher.replaceAll(match -> {
                try {
                    return rule.strategy.mask(match.group());
                } catch (Exception e) {
                    return match.group();
                }
            });
        }
        return result;
    }

    private record Rule(Pattern pattern, MaskingStrategy strategy) {
    }
}
