package org.hzw.context.bean.beanpostprocessor;

import org.hzw.context.annotation.Component;
import org.hzw.context.bean.BeanPostProcessor;
import org.hzw.context.exception.BeansException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hzw
 */
@Component
public class FirstBeanPostProcessor implements BeanPostProcessor {
    private final Map<String, Object> originalInstance = new HashMap<>();


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TransactionalBean) {
            originalInstance.put(beanName, bean);
            return new TransactionalBeanProxy((TransactionalBean) bean);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public Object postProcessOnSetProperty(Object bean, String beanName) {
        Object origin = originalInstance.get(beanName);
        if (origin != null && origin != bean) {
            return origin;
        }
        return bean;
    }

    static class TransactionalBeanProxy implements TransactionalBean {
        private final TransactionalBean instance;

        public TransactionalBeanProxy(TransactionalBean instance) {
            this.instance = instance;
        }


        public void doSomething() {
            System.out.println("begin tx");
            instance.doSomething();
            System.out.println("end tx");
        }
    }
}
