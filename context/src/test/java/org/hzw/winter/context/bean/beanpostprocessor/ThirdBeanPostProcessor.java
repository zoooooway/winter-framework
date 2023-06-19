package org.hzw.winter.context.bean.beanpostprocessor;

import org.hzw.winter.context.annotation.Component;
import org.hzw.winter.context.bean.BeanPostProcessor;
import org.hzw.winter.context.exception.BeansException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hzw
 */
@Component
public class ThirdBeanPostProcessor implements BeanPostProcessor {
    private final Map<String, Object> originalInstance = new HashMap<>();


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof OriginBean) {
            originalInstance.put(beanName, bean);
            return new ThirdBeanPostProcessor.OriginBeanProxy((OriginBean) bean);
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

    static class OriginBeanProxy extends OriginBean {
        private final OriginBean instance;

        public OriginBeanProxy(OriginBean instance) {
            this.instance = instance;
        }

        @Override
        public void doSomething() {
            System.out.println("before do something");
            instance.doSomething();
            System.out.println("after do something");
        }
    }
}
