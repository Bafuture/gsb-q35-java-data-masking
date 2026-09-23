package com.example.gsb.masking;

import com.example.gsb.masking.Fixtures.User;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapJsonOutputTest {

    private final MaskingEngine engine = new MaskingEngine();

    @Test
    void toMaskedMapProducesMaskedStructure() {
        Object result = engine.toMaskedMap(User.sample());

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertThat(map.get("phone")).isEqualTo("138****5678");
        assertThat(map.get("email")).isEqualTo("z***@example.com");
        assertThat(map.get("internalNote")).isEqualTo("do-not-mask");
        @SuppressWarnings("unchecked")
        Map<String, Object> address = (Map<String, Object>) map.get("address");
        assertThat(address.get("detail")).isEqualTo("北京市海淀区****");
        assertThat(address.get("city")).isEqualTo("北京");
    }

    @Test
    void toMaskedJsonProducesValidMaskedJson() {
        String json = engine.toMaskedJson(User.sample());

        assertThat(json).startsWith("{").endsWith("}");
        assertThat(json).contains("\"phone\":\"138****5678\"");
        assertThat(json).contains("\"email\":\"z***@example.com\"");
        assertThat(json).doesNotContain("13812345678");
        assertThat(json).doesNotContain("zhangsan@example.com");
        assertThat(json).contains("\"tags\":[\"vip\",\"internal\"]");
    }
}
