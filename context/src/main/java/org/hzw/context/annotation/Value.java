package org.hzw.context.annotation;

import java.lang.annotation.*;

/**
 * @author hzw
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {
    /**
     * 是否必须注入此依赖
     */
    String key() default "";
}
