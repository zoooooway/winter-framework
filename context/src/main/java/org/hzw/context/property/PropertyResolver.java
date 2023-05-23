package org.hzw.context.property;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author hzw
 */
public class PropertyResolver {
    private final Map<String, String> properties = new HashMap<>();


    public PropertyResolver() {
        this.properties.putAll(System.getenv());
    }

    public PropertyResolver(Properties props) {
        this.properties.putAll(System.getenv());
        Set<String> keys = props.stringPropertyNames();
        for (String k : keys) {
            this.properties.put(k, properties.get(k));
        }
    }

    public String getProperty(String key) {
        PropertyExpr propertyExpr = parsePropertyExpr(key);
        if (propertyExpr == null) {
            return properties.get(key);
        }

        String value = properties.getOrDefault(propertyExpr.getKey(), propertyExpr.getDefaultValue());
        if (value != null) {
            // 也许包含嵌套表达式: ${a.b:${c.d:e}}
            return parseValue(value);
        }

        return null;
    }


    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? defaultValue : null;
    }


    private PropertyExpr parsePropertyExpr(String key) {
        if (key.startsWith("${") && key.endsWith("}")) {
            int i = key.indexOf(":");
            if (i == -1) {
                // 没有默认值
                return new PropertyExpr(key, null);
            } else {
                return new PropertyExpr(key.substring(2, i), key.substring(i + 1, key.length() - 1));
            }
        }
        return null;
    }

    private String parseValue(String value) {
        PropertyExpr propertyExpr = parsePropertyExpr(value);
        if (propertyExpr == null) {
            return value;
        }

        return getProperty(propertyExpr.getKey(), propertyExpr.getDefaultValue());
    }
}
