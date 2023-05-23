package org.hzw.context.property;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author hzw
 */
public class PropertyResolver {
    private Map<String, String> properties = new HashMap<>();


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

        return properties.getOrDefault(propertyExpr.getKey(), propertyExpr.getDefaultValue());
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
}
