package org.hzw.context.annotation;

import java.lang.annotation.*;

/**
 * @author hzw
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {
    /**
     * 属性键
     */
    String value();
}
