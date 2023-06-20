package org.hzw.winter.aop.proxy;

import org.hzw.winter.context.annotation.ComponentScan;
import org.hzw.winter.context.bean.AnnotationConfigApplicationContext;
import org.hzw.winter.context.property.PropertyResolver;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author hzw
 */
@ComponentScan("org.hzw.winter")
public class ProxyResolverTest {

    @Test
    public void testProxy() {
        OriginBean originBean = new OriginBean();
        String name = "ash";
        originBean.setName(name);
        ProxyResolver pr = new ProxyResolver();
        OriginBean proxy = pr.createProxy(originBean, new PoliteInvocationHandler());
        assertEquals(proxy.sayHello(), "hello " + name + "!");
        assertEquals(proxy.sayGoodbye(), "goodbye " + name + ".");
    }

    @Test
    public void testAop() throws IOException, URISyntaxException, ClassNotFoundException {

        Map<String, Object> map = new HashMap<>();
        map.put("origin.name", "ash");
        map.put("transaction.name", "cli");
        PropertyResolver propertyResolver = new PropertyResolver(map);

        var context = new AnnotationConfigApplicationContext(ProxyResolverTest.class, propertyResolver);

        System.out.println("test BeanPostProcessor...");
        OriginBean originBean = context.getBean("originBean");
        assertEquals(originBean.sayHello(), "hello " + map.get("origin.name") + "!");
        assertEquals(originBean.sayGoodbye(), "goodbye " + map.get("origin.name") + ".");

        System.out.println("test transaction------");

        TransactionBean transactionBean = context.getBean("transactionBean");
        transactionBean.read();
        transactionBean.write();
    }
}
