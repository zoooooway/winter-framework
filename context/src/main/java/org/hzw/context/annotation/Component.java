package org.hzw.context.annotation;

import java.lang.annotation.*;

/**
 * 标记在类上，以指示容器将会调用该类的构造方法实例化一个bean
 *
 * @author hzw
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    /**
     * bean 名称，默认是首字母小写的类名
     */
    String name() default "";
}
