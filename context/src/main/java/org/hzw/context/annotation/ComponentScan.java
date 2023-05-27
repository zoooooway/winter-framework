package org.hzw.context.annotation;

import java.lang.annotation.*;

/**
 * 指定bean扫描的路径
 *
 * @author hzw
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComponentScan {
    /**
     * 扫描bean的路径，默认为当前类所在路径
     */
    String[] value() default {};
}
