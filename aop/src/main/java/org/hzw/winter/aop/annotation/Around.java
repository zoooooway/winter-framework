package org.hzw.winter.aop.annotation;

import java.lang.annotation.*;

/**
 * 标识此方法被代理
 *
 * @author hzw
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Around {

    /**
     * 指定handler名称
     */
    String value();
}
