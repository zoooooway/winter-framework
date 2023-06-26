package org.hzw.winter.jdbc.tx;

import jakarta.annotation.Nullable;
import org.hzw.winter.aop.proxy.ProxyBeanProcessor;
import org.hzw.winter.context.util.ClassUtils;

import java.lang.reflect.Method;

/**
 * 用于代理需要开启事务的BeanProcessor
 *
 * @author hzw
 */
public class TransactionBeanProcessor extends ProxyBeanProcessor<Transactional> {
    /**
     * 从bean的public方法上获取指示代理的注解
     */
    @Override
    @Nullable
    protected Transactional proxyAnno(Object bean, Class<Transactional> targetAnnoClass) {
        Method[] methods = bean.getClass().getMethods();
        for (Method m : methods) {
            Transactional annotation = ClassUtils.findAnnotation(m, targetAnnoClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }
}
