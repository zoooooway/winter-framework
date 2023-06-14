package org.hzw.context.bean.beanpostprocessor;

import org.hzw.context.annotation.Component;

/**
 * @author hzw
 */
@Component
public class LogBeanObj implements LogBean {
    @Override
    public void doSomething() {
        System.out.println("it is a log bean...");
    }
}
