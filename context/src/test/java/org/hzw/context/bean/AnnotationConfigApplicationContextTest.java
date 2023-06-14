package org.hzw.context.bean;

import org.hzw.context.bean.beanpostprocessor.LogBean;
import org.hzw.context.bean.beanpostprocessor.OriginBean;
import org.hzw.context.bean.beanpostprocessor.TransactionalBean;
import org.hzw.context.property.PropertyResolver;
import org.hzw.context.util.YamlUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * @author hzw
 */
public class AnnotationConfigApplicationContextTest {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void testScan() throws IOException, URISyntaxException, ClassNotFoundException {
        Properties properties = new Properties();
        URL resource = AnnotationConfigApplicationContextTest.class.getClassLoader().getResource("test.properties");
        assert resource != null;
        properties.load(Files.newInputStream(Path.of(resource.toURI())));
        PropertyResolver propertyResolver = new PropertyResolver(properties);

        YamlUtils yamlUtils = new YamlUtils();
        Map<String, Object> map = yamlUtils.loadYaml("test.yml");
        PropertyResolver ymlPropertyResolver = new PropertyResolver(map);

        var context = new AnnotationConfigApplicationContext(ScanApplication.class, ymlPropertyResolver);
        Map<String, BeanDefinition> beans = context.beans;
        beans.forEach((key, value) -> log.debug(value.toString()));

        BeanDefinition beanDefinition1 = beans.get("logBeanObj");
        LogBean instance1 = (LogBean) beanDefinition1.getInstance();
        instance1.doSomething();

        BeanDefinition beanDefinition2 = beans.get("transactionalBeanObj");
        TransactionalBean instance2 = (TransactionalBean) beanDefinition2.getInstance();
        instance2.doSomething();


        BeanDefinition beanDefinition3 = beans.get("mergeBeanObj");
        TransactionalBean instance3 = (TransactionalBean) beanDefinition3.getInstance();
        instance3.doSomething();

        BeanDefinition beanDefinition4 = beans.get("originBean");
        OriginBean instance4 = (OriginBean) beanDefinition4.getInstance();
        instance4.doSomething();

        context.close();
    }
}
