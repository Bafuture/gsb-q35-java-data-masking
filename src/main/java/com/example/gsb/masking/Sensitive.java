package com.example.gsb.masking;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注字段为敏感字段，并指定脱敏策略。
 *
 * <p>可重复注解：同一字段可声明多条 {@code @Sensitive}，通过 {@link #groups()}
 * 区分场景。引擎按声明顺序选择第一条「分组匹配」的规则；未声明分组的规则
 * 在任何场景下都匹配。</p>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Sensitives.class)
public @interface Sensitive {

    /** 脱敏策略实现类，需有 public 无参构造器。 */
    Class<? extends MaskingStrategy> strategy();

    /** 生效分组；为空表示所有场景均生效。 */
    String[] groups() default {};
}
