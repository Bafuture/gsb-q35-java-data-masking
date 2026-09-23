package com.example.gsb.masking;

/**
 * 脱敏策略 SPI。实现类必须提供 public 无参构造器，以便引擎按需实例化并缓存。
 */
@FunctionalInterface
public interface MaskingStrategy {

    /**
     * 对原始值进行脱敏。
     *
     * @param raw 原始值（字段值经 {@code String.valueOf} 转换而来，不会为 null）
     * @return 脱敏后的值
     */
    String mask(String raw);
}
