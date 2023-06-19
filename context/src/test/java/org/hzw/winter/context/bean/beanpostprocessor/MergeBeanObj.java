package org.hzw.winter.context.bean.beanpostprocessor;

import org.hzw.winter.context.annotation.Component;

/**
 * @author hzw
 */
// todo 实现多个接口的情况下，如果使用内部持有实例的代理方式，由于代理并不全部实现被代理对象的接口，因此后续代理将不会触发
@Component
public class MergeBeanObj implements LogBean, TransactionalBean {

    @Override
    public void doSomething() {
        System.out.println("it is a log bean and also a transactional bean");
    }
}
