package org.hzw.winter.context.annotation;

import java.lang.annotation.*;

/**
 * 指定bean的初始化顺序
 *
 * @author hzw
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Order {
    int value();
}
