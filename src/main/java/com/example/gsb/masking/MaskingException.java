package com.example.gsb.masking;

/** 脱敏库内部异常。 */
public class MaskingException extends RuntimeException {

    public MaskingException(String message, Throwable cause) {
        super(message, cause);
    }
}
