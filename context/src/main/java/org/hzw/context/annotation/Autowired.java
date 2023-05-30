package org.hzw.context.annotation;

import java.lang.annotation.*;

/**
 * @author hzw
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
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
