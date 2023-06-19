package org.hzw.winter.aop.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 抽象的环绕式切面处理
 *
 * @author hzw
 */
public abstract class AroundInvocationHandler implements InvocationHandler {

    abstract void before(Object proxy, Method method, Object[] args);

    abstract Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable;

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        before(proxy, method, args);

        Object result = doInvoke(proxy, method, args);

        after(proxy, result, method, args);

        return result;
    }

    abstract void after(Object proxy, Object result, Method method, Object[] args);
}
