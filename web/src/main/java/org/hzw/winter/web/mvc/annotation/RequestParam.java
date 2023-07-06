package org.hzw.winter.web.mvc.annotation;

import java.lang.annotation.*;

/**
 * @author hzw
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface RequestParam {
    /**
     * 参数名称
     */
    String value() default "";

    String defaultValue() default "";
}
