package org.hzw.context.annotation;

import java.lang.annotation.*;

/**
 * 当多个相同类型bean优先注入注解标记的类
 *
 * @author hzw
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Primary {
}
