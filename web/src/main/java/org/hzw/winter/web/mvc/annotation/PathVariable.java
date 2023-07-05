package org.hzw.winter.web.mvc.annotation;

import java.lang.annotation.*;

/**
 * @author hzw
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface PathVariable {

    /**
     * 请求参数名称
     */
    String value() default "";
}
