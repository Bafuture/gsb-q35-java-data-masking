package com.example.gsb.masking;

import com.example.gsb.masking.strategy.AddressMaskingStrategy;
import com.example.gsb.masking.strategy.BankCardMaskingStrategy;
import com.example.gsb.masking.strategy.EmailMaskingStrategy;
import com.example.gsb.masking.strategy.IdCardMaskingStrategy;
import com.example.gsb.masking.strategy.NameMaskingStrategy;
import com.example.gsb.masking.strategy.PhoneMaskingStrategy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Fixtures {

    private Fixtures() {
    }

    static class Address {
        @Sensitive(strategy = AddressMaskingStrategy.class)
        String detail;
        String city;

        Address() {
        }

        Address(String detail, String city) {
            this.detail = detail;
            this.city = city;
        }
    }

    static class User {
        @Sensitive(strategy = PhoneMaskingStrategy.class)
        String phone;
        @Sensitive(strategy = IdCardMaskingStrategy.class)
        String idCard;
        @Sensitive(strategy = BankCardMaskingStrategy.class)
        String bankCard;
        @Sensitive(strategy = EmailMaskingStrategy.class)
        String email;
        @Sensitive(strategy = NameMaskingStrategy.class)
        String name;
        Address address;
        List<Address> addresses = new ArrayList<>();
        Map<String, Address> addressMap = new LinkedHashMap<>();
        String[] tags;
        User friend;
        @SensitiveIgnore
        String internalNote;

        static User sample() {
            User user = new User();
            user.phone = "13812345678";
            user.idCard = "110101199001011234";
            user.bankCard = "6222021234567891234";
            user.email = "zhangsan@example.com";
            user.name = "欧阳娜娜";
            user.address = new Address("北京市海淀区中关村大街27号", "北京");
            user.addresses.add(new Address("上海市浦东新区世纪大道1号", "上海"));
            user.addressMap.put("home", new Address("广州市天河区体育西路5号", "广州"));
            user.tags = new String[]{"vip", "internal"};
            user.internalNote = "do-not-mask";
            return user;
        }
    }
}
