package org.hzw.winter.web.mvc.annotation;

import java.lang.annotation.*;

/**
 * 指示返回值应直接写入响应
 *
 * @author hzw
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface ResponseBody {
}
