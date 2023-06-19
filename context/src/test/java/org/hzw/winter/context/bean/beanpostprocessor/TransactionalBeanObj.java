package org.hzw.winter.context.bean.beanpostprocessor;

import org.hzw.winter.context.annotation.Component;

/**
 * @author hzw
 */
@Component
public class TransactionalBeanObj implements TransactionalBean {
    @Override
    public void doSomething() {
        System.out.println("it is a transactional bean...");
    }
}
