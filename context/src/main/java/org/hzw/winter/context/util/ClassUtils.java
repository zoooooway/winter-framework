package org.hzw.winter.context.util;

import jakarta.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * @author hzw
 */
public class ClassUtils {

    /**
     * 递归的寻找是否存在指定的注解
     *
     * @param source     被搜寻者
     * @param annotation 搜寻的目标注解
     * @return 搜寻到的注解，未找到则为null
     */
    @Nullable
    public static <A extends Annotation> A findAnnotation(AnnotatedElement source, Class<A> annotation) {
        // 是否存在了bean注解
        Annotation[] annotations = source.getAnnotations();
        for (Annotation anno : annotations) {
            // 避免无限递归， 比如@Target注解自身标记自身
            if (anno.annotationType() == source) {
                return null;
            }
            // anno.getClass() 会返回 proxy class 而不是 Annotation的真实 class，
            if (anno.annotationType() == annotation) {
                return source.getAnnotation(annotation);
            }

            A a = findAnnotation(anno.annotationType(), annotation);
            if (a != null) {
                return a;
            }
        }
        return null;
    }
}
