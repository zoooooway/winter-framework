package org.hzw.winter.web.mvc.annotation;

import org.hzw.winter.context.annotation.Component;

import java.lang.annotation.*;

/**
 * @author hzw
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Component
public @interface Controller {

    /**
     * bean名称，默认为首字母小写的类名
     */
    String value() default "";
}
