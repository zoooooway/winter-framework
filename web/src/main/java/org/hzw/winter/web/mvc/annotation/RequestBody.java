package org.hzw.winter.web.mvc.annotation;

import java.lang.annotation.*;

/**
 * 指示从body中获取请求参数
 *
 * @author hzw
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface RequestBody {
}
