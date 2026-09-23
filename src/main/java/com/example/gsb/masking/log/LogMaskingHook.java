package com.example.gsb.masking.log;

/**
 * 日志格式化钩子。日志框架适配层（如 Logback Converter、Log4j2 PatternConverter）
 * 在输出前调用 {@link #mask(String)} 对消息文本脱敏。
 *
 * <p>实现需线程安全。接入示例见 README。</p>
 */
@FunctionalInterface
public interface LogMaskingHook {

    /**
     * 对一条日志消息脱敏。
     *
     * @param message 已格式化的日志消息
     * @return 脱敏后的消息；实现应保证不抛异常、不返回 null
     */
    String mask(String message);
}
