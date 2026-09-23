package com.example.gsb.masking;

import com.example.gsb.masking.Fixtures.Address;
import com.example.gsb.masking.Fixtures.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RecursiveMaskingTest {

    private final MaskingEngine engine = new MaskingEngine();

    @Test
    void masksNestedCollectionsMapsAndArrays() {
        User user = User.sample();
        User masked = engine.mask(user);

        assertThat(masked.address.detail).isEqualTo("北京市海淀区****");
        assertThat(masked.address.city).isEqualTo("北京");
        assertThat(masked.addresses.get(0).detail).isEqualTo("上海市浦东新****");
        assertThat(masked.addressMap.get("home").detail).isEqualTo("广州市天河区****");
        assertThat(masked.tags).containsExactly("vip", "internal");
    }

    @Test
    void handlesCircularReferenceWithoutStackOverflow() {
        User a = User.sample();
        User b = User.sample();
        b.name = "李四";
        a.friend = b;
        b.friend = a;

        User masked = engine.mask(a);

        assertThat(masked.friend.name).isEqualTo("李*");
        assertThat(masked.friend.friend).isSameAs(masked);
        assertThat(masked.friend.friend.phone).isEqualTo("138****5678");
    }

    @Test
    void mapOutputMarksCircularReference() {
        User a = User.sample();
        a.friend = a;

        Object result = engine.toMaskedMap(a);

        assertThat(result).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertThat(map.get("phone")).isEqualTo("138****5678");
        assertThat(map.get("friend")).isEqualTo(MaskingEngine.CIRCULAR_MARKER);
        assertThat(map.get("address")).isInstanceOf(Map.class);
        assertThat(map.get("addresses")).isInstanceOf(List.class);
    }
}
