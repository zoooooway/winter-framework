package org.hzw.winter.context.property;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.hzw.winter.context.exception.UnsupportedFileException;
import org.hzw.winter.context.util.ClassUtils;
import org.hzw.winter.context.util.YamlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Nonnull
    public static PropertyResolver create(@Nonnull String path) throws IOException, URISyntaxException {
        PropertyResolver propertyResolver = new PropertyResolver();
        if (path.endsWith(".yml")) {
            Map<String, Object> map = YamlUtils.loadYaml(path);
            fillProps(map, propertyResolver);

        } else if (path.endsWith(".properties")) {
            Properties props = new Properties();
            URL resource = ClassUtils.getContextClassLoader().getResource(path);
            assert resource != null;
            try (InputStream is = Files.newInputStream(Path.of(resource.toURI()))) {
                props.load(is);
                fillProps(props, propertyResolver);
            }

        } else {
            throw new UnsupportedFileException();
        }

        initializeConverters(propertyResolver.converters);
        return propertyResolver;
    }

    public PropertyResolver(Properties props) {
        this.properties.putAll(System.getenv());
        fillProps(props, this);
        initializeConverters(converters);
    }

    public PropertyResolver(Map<String, Object> props) {
        this.properties.putAll(System.getenv());
        fillProps(props, this);
        initializeConverters(converters);
    }

    private static void fillProps(Properties props, PropertyResolver pr) {
        Set<String> keys = props.stringPropertyNames();
        for (String k : keys) {
            pr.properties.put(k, props.getProperty(k));
        }
    }

    private static void fillProps(Map<String, Object> props, PropertyResolver pr) {
        Set<String> keys = props.keySet();
        for (String k : keys) {
            pr.properties.put(k, props.get(k).toString());
        }
    }

    private static void initializeConverters(Map<Class<?>, Function<String, Object>> coverters) {
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
            String val = getProperty(key);
            if (val == null) {
                return null;
            }
            Function<String, Object> convertFunc = converters.get(targetType);
            return (T) convertFunc.apply(val);
        }

        String value = this.properties.getOrDefault(propertyExpr.getKey(), propertyExpr.getDefaultValue());
        if (value != null) {
            // 也许包含嵌套表达式, 比如: "${a.b:${c.d:e}}"
            return parseValue(value, targetType);
        }
        return null;
    }


    @Nonnull
    private String getProperty(String key) {
        return this.properties.get(key);
    }

    @Nonnull
    public <T> T getRequiredProperty(String key, Class<T> targetType) {
        T property = getProperty(key, targetType);

        return Objects.requireNonNull(property, "Property '" + key + "' not found.");
    }

    @Nullable
    private <T> T getProperty(String key, String defaultValue, Class<T> targetType) {
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
     * @param func  转换函数
     */
    public void registerConverter(Class<?> clazz, Function<String, Object> func) {
        converters.put(clazz, func);
    }
}
