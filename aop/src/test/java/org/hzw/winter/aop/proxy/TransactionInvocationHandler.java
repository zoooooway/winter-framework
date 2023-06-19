package org.hzw.winter.aop.proxy;

import org.hzw.winter.context.annotation.Component;

import java.lang.reflect.Method;

/**
 * @author hzw
 */
@Component
public class TransactionInvocationHandler extends AroundInvocationHandler {
    @Override
    public void before(Object proxy, Method method, Object[] args) {
        if (method.getAnnotation(Transaction.class) != null) {
            System.out.println("before transaction proxy...");
        }

    }


    @Override
    public void after(Object proxy, Object result, Method method, Object[] args) {
        if (method.getAnnotation(Transaction.class) != null) {
            System.out.println("after transaction proxy... result: " + result);
        }
    }

    @Override
    public Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getAnnotation(Transaction.class) != null) {
            System.out.println("begin transaction...");
            Object invoke = method.invoke(proxy, args);
            System.out.println("commit transaction...");
            return invoke;

        } else {
            return method.invoke(proxy, args);
        }

    }

}
