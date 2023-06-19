package org.hzw.winter.context.property;

/**
 * 为了解析形如 ${key:defaultValue} 的注入值，在此类中暂存 default value
 *
 * @author hzw
 */
public class PropertyExpr {
    private final String key;
    private final String defaultValue;

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
