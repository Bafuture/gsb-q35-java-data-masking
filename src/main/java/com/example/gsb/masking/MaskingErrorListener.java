package com.example.gsb.masking;

import java.util.logging.Level;
import java.util.logging.Logger;

/** 脱敏异常监听器；引擎保证单个异常不会中断整体流程，而是回调此接口。 */
@FunctionalInterface
public interface MaskingErrorListener {

    void onError(MaskingError error);

    /** 默认实现：输出到 java.util.logging。 */
    static MaskingErrorListener julLogger() {
        Logger logger = Logger.getLogger(MaskingErrorListener.class.getName());
        return error -> logger.log(Level.WARNING, "masking failed: " + error, error.getCause());
    }
}
