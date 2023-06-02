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
public @interface Autowired {
    /**
     * 是否必须注入此依赖
     */
    boolean required() default true;

    /**
     * 指定注入依赖的名称
     */
    String name() default "";
}
