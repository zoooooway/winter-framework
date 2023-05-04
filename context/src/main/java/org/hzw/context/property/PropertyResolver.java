package org.hzw.context.property;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hzw
 */
public class PropertyResolver {
    private Map<String, String> propers = new HashMap<>();


    public PropertyResolver() {
        this.propers.putAll(System.getenv());
    }

    public String getProperty(String key) {
        return propers.get(key);
    }
}
