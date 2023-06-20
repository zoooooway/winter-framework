package org.hzw.winter.aop.proxy;

import jakarta.annotation.Nullable;
import org.hzw.winter.context.annotation.Component;
import org.hzw.winter.context.util.ClassUtil;

import java.lang.reflect.Method;

/**
 * @author hzw
 */
@Component
public class TransactionBeanProcessor extends ProxyBeanProcessor<Transaction> {

    /**
     * 从bean的public方法上获取指示代理的注解
     */
    @Override
    @Nullable
    protected Transaction proxyAnno(Object bean, Class<Transaction> targetAnnoClass) {
        Method[] methods = bean.getClass().getMethods();
        for (Method m : methods) {
            Transaction annotation = ClassUtil.findAnnotation(m, targetAnnoClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }
}
