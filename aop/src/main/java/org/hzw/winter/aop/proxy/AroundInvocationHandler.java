package org.hzw.winter.aop.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 抽象的环绕式切面处理
 *
 * @author hzw
 */
public interface AroundInvocationHandler extends InvocationHandler {

    default void before(Object proxy, Method method, Object[] args) {
        // do nothing
    }

    Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable;

    @Override
    default Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        before(proxy, method, args);

        Object result = doInvoke(proxy, method, args);

        after(proxy, result, method, args);

        return result;
    }

    default void after(Object proxy, Object result, Method method, Object[] args) {
        // do nothing
    }
}
