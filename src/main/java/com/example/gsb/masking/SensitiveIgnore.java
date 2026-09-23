package com.example.gsb.masking;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注字段不参与脱敏处理：即使字段本身或其内部对象带有 {@link Sensitive}
 * 注解，也按原样保留。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveIgnore {
}
