package com.example.gsb.masking;

import com.example.gsb.masking.Fixtures.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CopyImmutabilityTest {

    private final MaskingEngine engine = new MaskingEngine();

    @Test
    void maskDoesNotModifyOriginalObject() {
        User user = User.sample();

        User masked = engine.mask(user);

        assertThat(masked).isNotSameAs(user);
        assertThat(user.phone).isEqualTo("13812345678");
        assertThat(user.idCard).isEqualTo("110101199001011234");
        assertThat(user.bankCard).isEqualTo("6222021234567891234");
        assertThat(user.email).isEqualTo("zhangsan@example.com");
        assertThat(user.name).isEqualTo("欧阳娜娜");
        assertThat(user.address.detail).isEqualTo("北京市海淀区中关村大街27号");
        assertThat(user.addresses.get(0).detail).isEqualTo("上海市浦东新区世纪大道1号");
        assertThat(user.addressMap.get("home").detail).isEqualTo("广州市天河区体育西路5号");
    }

    @Test
    void maskedCopyIsDeepAndIndependent() {
        User user = User.sample();

        User masked = engine.mask(user);

        assertThat(masked.address).isNotSameAs(user.address);
        assertThat(masked.addresses).isNotSameAs(user.addresses);
        assertThat(masked.addresses.get(0)).isNotSameAs(user.addresses.get(0));
        assertThat(masked.addressMap).isNotSameAs(user.addressMap);
        assertThat(masked.tags).isNotSameAs(user.tags);

        masked.addresses.get(0).city = "被篡改";
        assertThat(user.addresses.get(0).city).isEqualTo("上海");
    }

    @Test
    void maskedCopyContainsMaskedValues() {
        User masked = engine.mask(User.sample());

        assertThat(masked.phone).isEqualTo("138****5678");
        assertThat(masked.idCard).isEqualTo("110101********1234");
        assertThat(masked.bankCard).isEqualTo("***************1234");
        assertThat(masked.email).isEqualTo("z***@example.com");
        assertThat(masked.name).isEqualTo("欧***");
        assertThat(masked.internalNote).isEqualTo("do-not-mask");
    }
}
