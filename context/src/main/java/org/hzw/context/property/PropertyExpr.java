package org.hzw.context.property;

/**
 * @author hzw
 */
public class PropertyExpr {
    private String key;
    private String defaultValue;

    public PropertyExpr(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
