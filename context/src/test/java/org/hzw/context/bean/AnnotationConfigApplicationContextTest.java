package org.hzw.context.bean;

import org.hzw.context.property.PropertyResolver;
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
        var context = new AnnotationConfigApplicationContext(ScanApplication.class, propertyResolver);
        Map<String, BeanDefinition> beans = context.beans;
        beans.forEach((key, value) -> log.debug(value.toString()));
    }
}
