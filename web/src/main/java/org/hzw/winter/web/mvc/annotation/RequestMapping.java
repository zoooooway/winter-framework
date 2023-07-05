package org.hzw.winter.web.mvc.annotation;

import java.lang.annotation.*;

/**
 * 搭配@Controller标注在类上使用，指示该类应该处理的url路径
 *
 * @author hzw
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RequestMapping {
    /**
     * 请求路径
     */
    String value() default "";
}
