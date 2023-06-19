package org.hzw.winter.aop.proxy;

import org.hzw.winter.context.annotation.Component;

import java.lang.reflect.Method;

/**
 * @author hzw
 */
@Component
public class PoliteInvocationHandler extends AroundInvocationHandler {
    @Override
    public void before(Object proxy, Method method, Object[] args) {
        if (method.getAnnotation(Polite.class) != null) {
            System.out.println("before proxy...");
        }

    }


    @Override
    public void after(Object proxy, Object result, Method method, Object[] args) {
        if (method.getAnnotation(Polite.class) != null) {
            System.out.println("after proxy... result: " + result);
        }
    }

    @Override
    public Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getAnnotation(Polite.class) != null) {
            Object invoke = method.invoke(proxy, args);
            if (invoke instanceof String) {
                String originRes = (String)invoke;
                if (originRes.endsWith(".")) {
                    return originRes.substring(0, originRes.length() - 1) + "!";
                }
            }
            return invoke;
        } else {
            return method.invoke(proxy, args);
        }


    }

}
