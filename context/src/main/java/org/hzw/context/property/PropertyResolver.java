package org.hzw.context.property;

import jakarta.annotation.Nullable;

import java.time.*;
import java.util.*;
import java.util.function.Function;

/**
 * 注入属性的解析器，支持普通键值解析和嵌套键值的解析
 *
 * @author hzw
 */
public class PropertyResolver {
    private final Map<String, String> properties = new HashMap<>();
    /**
     * 转换器集合
     */
    private final Map<Class<?>, Function<String, Object>> converters = new HashMap<>();


    public PropertyResolver() {
        this.properties.putAll(System.getenv());
        initializeConverters(converters);
    }

    public PropertyResolver(Properties props) {
        this.properties.putAll(System.getenv());
        Set<String> keys = props.stringPropertyNames();
        for (String k : keys) {
            this.properties.put(k, props.getProperty(k));
        }
        initializeConverters(converters);
    }

    private void initializeConverters(Map<Class<?>, Function<String, Object>> coverters) {
        // 基本数据类型
        coverters.put(int.class, Integer::parseInt);
        coverters.put(Integer.class, Integer::valueOf);
        coverters.put(long.class, Long::parseLong);
        coverters.put(Long.class, Long::valueOf);
        coverters.put(double.class, Double::parseDouble);
        coverters.put(Double.class, Double::valueOf);
        coverters.put(float.class, Float::parseFloat);
        coverters.put(Float.class, Float::valueOf);
        coverters.put(boolean.class, Boolean::parseBoolean);
        coverters.put(Boolean.class, Boolean::valueOf);
        coverters.put(String.class, String::valueOf);
        // 时间
        coverters.put(Date.class, Date::parse);
        coverters.put(LocalDate.class, LocalDate::parse);
        coverters.put(LocalDateTime.class, LocalDateTime::parse);
        coverters.put(ZonedDateTime.class, ZonedDateTime::parse);
        coverters.put(Duration.class, Duration::parse);
        coverters.put(ZoneId.class, ZoneId::of);

    }

    @Nullable
    public <T> T getProperty(String key, Class<T> targetType) {
        PropertyExpr propertyExpr = parsePropertyExpr(key);
        if (propertyExpr == null) {
            String val = properties.get(key);
            if (val == null) {
                return null;
            }
            Function<String, Object> convertFunc = converters.get(targetType);
            return (T) convertFunc.apply(val);
        }

        String value = properties.getOrDefault(propertyExpr.getKey(), propertyExpr.getDefaultValue());
        if (value != null) {
            // 也许包含嵌套表达式, 比如: "${a.b:${c.d:e}}"
            return parseValue(value, targetType);
        }

        return null;
    }


    @Nullable
    public <T> T getProperty(String key, String defaultValue, Class<T> targetType) {
        Object property = getProperty(key, targetType);
        if (property != null) {
            return (T) property;
        }

        if (defaultValue != null) {
            return (T) converters.get(targetType).apply(defaultValue);
        }

        return null;
    }

    @Nullable
    private PropertyExpr parsePropertyExpr(String key) {
        if (key.startsWith("${") && key.endsWith("}")) {
            int i = key.indexOf(":");
            if (i == -1) {
                // 没有默认值
                return new PropertyExpr(key.substring(2, key.length() - 1), null);
            } else {
                return new PropertyExpr(key.substring(2, i), key.substring(i + 1, key.length() - 1));
            }
        }
        return null;
    }

    private <T> T parseValue(String value, Class<T> targetType) {
        PropertyExpr propertyExpr = parsePropertyExpr(value);
        if (propertyExpr == null) {
            return (T) converters.get(targetType).apply(value);
        }

        return getProperty(propertyExpr.getKey(), propertyExpr.getDefaultValue(), targetType);
    }

    /**
     * 注册自定义的convert到当前PropertyResolver实例中
     *
     * @param clazz 目标类型
     * @param func 转换函数
     */
    public void registerConverter(Class<?> clazz, Function<String, Object> func) {
        converters.put(clazz, func);
    }
}
