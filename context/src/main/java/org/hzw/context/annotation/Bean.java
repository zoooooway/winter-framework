package org.hzw.context.annotation;

import java.lang.annotation.*;

/**
 * 标记在方法上，以指示容器将调用该方法实例化一个bean
 *
 * @author hzw
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {
    String name() default "";

    String initMethod() default "";

    String destroyMethod() default "";
}
