package org.hzw.winter.aop.proxy;

import org.hzw.winter.aop.annotation.Around;
import org.hzw.winter.context.annotation.Component;
import org.hzw.winter.context.annotation.Value;

/**
 * @author hzw
 */
@Around("transactionInvocationHandler")
@Component
public class TransactionBean {
    @Value("${transaction.name}")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void read() {
        System.out.println("read" + name + " ...");
    }

    @Transaction
    public void write() {
        System.out.println("write " + name + " ...");
    }
}
