package org.hzw.context.bean.beanpostprocessor;

import org.hzw.context.annotation.Component;

/**
 * @author hzw
 */
@Component
public class MergeBeanObj implements LogBean, TransactionalBean {

    @Override
    public void doSomething() {
        System.out.println("it is a log bean and also a transactional bean");
    }
}
