package org.hzw.winter.aop.proxy;

import java.lang.annotation.*;

/**
 * test annotation
 *
 * @author hzw
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transaction {
    String value();
}
