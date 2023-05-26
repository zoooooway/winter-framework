package org.hzw.context.property;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author hzw
 */
public class PropertyResolverTest {

    @Test
    public void testResolver() throws IOException, URISyntaxException {
        Properties properties = new Properties();
        URL resource = PropertyResolverTest.class.getClassLoader().getResource("test.properties");
        assert resource != null;
        properties.load(Files.newInputStream(Path.of(resource.toURI())));
        var resolver = new PropertyResolver(properties);

        assertEquals(properties.getProperty("student.name"), resolver.getProperty("student.name"));
        assertEquals(properties.getProperty("student.name"), resolver.getProperty("${student.name}"));
        assertEquals(properties.getProperty("student.name"), resolver.getProperty("${student.name:noop}"));
        assertEquals(properties.getProperty("teacher.name"), resolver.getProperty("${boss.name:${teacher.name}}"));
    }
}
