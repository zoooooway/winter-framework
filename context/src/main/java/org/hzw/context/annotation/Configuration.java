package org.hzw.context.annotation;

import java.lang.annotation.*;

/**
 * 标记在类上，以指示容器将会调用该类的构造方法实例化一个bean，并且调用类中所有被@Bean标记的方法实例化对应的bean
 *
 * @author hzw
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {
}
