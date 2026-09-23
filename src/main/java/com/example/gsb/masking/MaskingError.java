package com.example.gsb.masking;

/** 一次脱敏异常的记录。 */
public final class MaskingError {

    private final String path;
    private final Class<?> ownerType;
    private final Throwable cause;

    public MaskingError(String path, Class<?> ownerType, Throwable cause) {
        this.path = path;
        this.ownerType = ownerType;
        this.cause = cause;
    }

    /** 出错位置，例如 {@code "user.address.detail"} 或 {@code "users[2].phone"}。 */
    public String getPath() {
        return path;
    }

    /** 出错字段所属类型，无法定位时为 null。 */
    public Class<?> getOwnerType() {
        return ownerType;
    }

    public Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return "MaskingError{path='" + path + "', owner="
                + (ownerType == null ? "?" : ownerType.getSimpleName())
                + ", cause=" + cause + '}';
    }
}
