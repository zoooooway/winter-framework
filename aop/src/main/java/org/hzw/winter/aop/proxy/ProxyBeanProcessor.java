package org.hzw.winter.aop.proxy;

import org.hzw.winter.aop.exception.AopException;
import org.hzw.winter.context.bean.BeanDefinition;
import org.hzw.winter.context.bean.BeanPostProcessor;
import org.hzw.winter.context.bean.ConfigurableApplicationContext;
import org.hzw.winter.context.exception.BeansException;
import org.hzw.winter.context.util.ApplicationContextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hzw
 */
public class ProxyBeanProcessor<A extends Annotation> implements BeanPostProcessor {
    private final Map<String, Object> originBean = new HashMap<>();
    private Class<A> annotationClass = getParameterizedType();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        A annotation = bean.getClass().getAnnotation(annotationClass);
        if (annotation != null) {
            String handlerName;
            try {
                handlerName = (String) annotation.getClass().getMethod("value").invoke(annotation);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new BeansException(e);
            }

            ConfigurableApplicationContext context = (ConfigurableApplicationContext) ApplicationContextUtils.getRequiredApplicationContext();

            BeanDefinition handlerBdf = context.findBeanDefinition(handlerName);
            if (handlerBdf.getInstance() == null) {
                context.createBeanAsEarlySingleton(handlerBdf);
            }

            if (handlerBdf.getInstance() instanceof InvocationHandler) {
                Object proxy = ProxyResolver.getInstance().createProxy(bean, (InvocationHandler) handlerBdf.getInstance());
                originBean.put(beanName, bean);
                return proxy;
            } else {
                throw new AopException(String.format("Expected a bean of type 'InvocationHandler', but could not be found. bean: '%s'", handlerName));
            }
        }

        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessOnSetProperty(Object bean, String beanName) {
        Object origin = originBean.get(beanName);
        if (origin == null || origin == bean) {
            return bean;
        }
        return origin;
    }

    @SuppressWarnings("unchecked")
    private Class<A> getParameterizedType() {
        Type type = getClass().getGenericSuperclass();
        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " does not have parameterized type.");
        }
        ParameterizedType pt = (ParameterizedType) type;
        Type[] types = pt.getActualTypeArguments();
        if (types.length != 1) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " has more than 1 parameterized types.");
        }
        Type r = types[0];
        if (!(r instanceof Class<?>)) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " does not have parameterized type of class.");
        }
        return (Class<A>) r;
    }
}