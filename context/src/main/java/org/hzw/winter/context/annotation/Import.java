package org.hzw.winter.context.annotation;

import java.lang.annotation.*;

/**
 * 指定导入Bean
 *
 * @author hzw
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import {
    Class<?>[] value() default {};
}
