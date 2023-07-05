package org.hzw.winter.web.mvc.annotation;

import java.lang.annotation.*;

/**
 * @author hzw
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface PostMapping {
    /**
     * 请求路径
     */
    String value() default "";
}
