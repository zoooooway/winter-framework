package org.hzw.context.annotation;

import java.lang.annotation.*;

/**
 * 支持在字段和方法参数上注入依赖的注解
 *
 * @author hzw
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {
    /**
     * 属性键
     */
    String value();
}
