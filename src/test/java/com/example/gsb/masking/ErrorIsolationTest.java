package com.example.gsb.masking;

import com.example.gsb.masking.strategy.PhoneMaskingStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorIsolationTest {

    public static class BoomStrategy implements MaskingStrategy {
        @Override
        public String mask(String raw) {
            throw new IllegalStateException("boom");
        }
    }

    static class NoDefaultConstructor {
        String value = "x";

        NoDefaultConstructor(String value) {
            this.value = value;
        }
    }

    static class Doc {
        @Sensitive(strategy = BoomStrategy.class)
        String secret = "top-secret";
        @Sensitive(strategy = PhoneMaskingStrategy.class)
        String phone = "13812345678";
        NoDefaultConstructor tricky = new NoDefaultConstructor("raw");
    }

    @Test
    void placeholderModeReplacesFailedValueAndContinues() {
        List<MaskingError> errors = new ArrayList<>();
        MaskingEngine engine = new MaskingEngine(MaskingConfig.builder()
                .onError(OnError.PLACEHOLDER)
                .placeholder("<masked>")
                .errorListener(errors::add)
                .build());

        Doc masked = engine.mask(new Doc());

        assertThat(masked.secret).isEqualTo("<masked>");
        assertThat(masked.phone).isEqualTo("138****5678");
        assertThat(errors).isNotEmpty();
        assertThat(errors.get(0).getPath()).contains("secret");
        assertThat(errors.get(0).getCause()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void keepOriginalModeRetainsFailedValueAndContinues() {
        List<MaskingError> errors = new ArrayList<>();
        MaskingEngine engine = new MaskingEngine(MaskingConfig.builder()
                .onError(OnError.KEEP_ORIGINAL)
                .errorListener(errors::add)
                .build());

        Doc masked = engine.mask(new Doc());

        assertThat(masked.secret).isEqualTo("top-secret");
        assertThat(masked.phone).isEqualTo("138****5678");
        assertThat(errors).isNotEmpty();
    }

    @Test
    void instantiationFailureIsRecordedAndDoesNotAbort() {
        List<MaskingError> errors = new ArrayList<>();
        MaskingEngine engine = new MaskingEngine(MaskingConfig.builder()
                .errorListener(errors::add)
                .build());

        Doc masked = engine.mask(new Doc());

        assertThat(masked.phone).isEqualTo("138****5678");
        assertThat(masked.tricky).isNotNull();
        assertThat(errors).anySatisfy(error ->
                assertThat(error.getPath()).contains("tricky"));
    }
}
