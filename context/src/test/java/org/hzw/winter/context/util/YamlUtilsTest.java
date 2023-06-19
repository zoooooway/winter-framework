package org.hzw.winter.context.util;

import org.hzw.winter.context.util.YamlUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author hzw
 */
public class YamlUtilsTest {

    @Test
    public void testYaml() throws IOException, URISyntaxException {
        YamlUtils yamlUtils = new YamlUtils();
        Map<String, Object> map = yamlUtils.loadYaml("test.yml");
        assertEquals(map.get("student.name"), "小明");
        assertEquals(map.get("student.age"), "12");
        assertEquals(map.get("teacher.name"), "大明");
        assertEquals(map.get("teacher.age"), "33");
    }
}
