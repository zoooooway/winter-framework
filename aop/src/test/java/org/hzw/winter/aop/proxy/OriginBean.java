package org.hzw.winter.aop.proxy;

import org.hzw.winter.aop.annotation.Around;
import org.hzw.winter.context.annotation.Component;
import org.hzw.winter.context.annotation.Value;

/**
 * @author hzw
 */
@Around("politeInvocationHandler")
@Component
public class OriginBean {
    @Value("${origin.name}")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Polite
    public String sayHello() {
        return "hello " + name + ".";
    }


    public String sayGoodbye() {
        return "goodbye " + name + ".";
    }
}
