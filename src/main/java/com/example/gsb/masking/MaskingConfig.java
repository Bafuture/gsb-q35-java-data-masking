package com.example.gsb.masking;

import java.util.LinkedHashSet;
import java.util.Set;

/** 脱敏引擎配置。通过 {@link #builder()} 构建。 */
public final class MaskingConfig {

    public static final String DEFAULT_PLACEHOLDER = "******";

    private final Set<String> activeGroups;
    private final OnError onError;
    private final String placeholder;
    private final MaskingErrorListener errorListener;

    private MaskingConfig(Builder builder) {
        this.activeGroups = Set.copyOf(builder.activeGroups);
        this.onError = builder.onError;
        this.placeholder = builder.placeholder;
        this.errorListener = builder.errorListener;
    }

    public static MaskingConfig defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /** 当前启用的分组；空集表示未按分组过滤（仅无分组注解生效）。 */
    public Set<String> getActiveGroups() {
        return activeGroups;
    }

    public OnError getOnError() {
        return onError;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public MaskingErrorListener getErrorListener() {
        return errorListener;
    }

    public static final class Builder {

        private final Set<String> activeGroups = new LinkedHashSet<>();
        private OnError onError = OnError.KEEP_ORIGINAL;
        private String placeholder = DEFAULT_PLACEHOLDER;
        private MaskingErrorListener errorListener = MaskingErrorListener.julLogger();

        private Builder() {
        }

        /** 启用指定分组，例如日志场景 {@code "log"}、审计场景 {@code "audit"}。 */
        public Builder activeGroups(String... groups) {
            for (String group : groups) {
                if (group != null && !group.isBlank()) {
                    activeGroups.add(group);
                }
            }
            return this;
        }

        public Builder onError(OnError onError) {
            this.onError = onError == null ? OnError.KEEP_ORIGINAL : onError;
            return this;
        }

        public Builder placeholder(String placeholder) {
            this.placeholder = placeholder == null ? DEFAULT_PLACEHOLDER : placeholder;
            return this;
        }

        public Builder errorListener(MaskingErrorListener errorListener) {
            this.errorListener = errorListener == null ? MaskingErrorListener.julLogger() : errorListener;
            return this;
        }

        public MaskingConfig build() {
            return new MaskingConfig(this);
        }
    }
}
