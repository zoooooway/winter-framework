package org.hzw.winter.jdbc.tx;

import java.lang.annotation.*;

/**
 * 声明此方法启用事务管理
 *
 * @author hzw
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transactional {

    /**
     * 指定负责事务管理的bean名称
     */
    String value() default "simpleTransactionManager";
}
