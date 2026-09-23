package com.example.gsb.masking.strategy;

/** 内置策略共用的工具方法。 */
final class MaskingSupport {

    private MaskingSupport() {
    }

    /** 保留头部 head 个、尾部 tail 个字符，中间用 {@code *} 替换；长度不足时全部替换。 */
    static String maskMiddle(String raw, int head, int tail) {
        if (raw == null) {
            return null;
        }
        int len = raw.length();
        if (len <= head + tail) {
            return "*".repeat(len);
        }
        return raw.substring(0, head) + "*".repeat(len - head - tail) + raw.substring(len - tail);
    }
}
