package org.hzw.winter.context.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author hzw
 */
public class YamlUtils {


    /**
     * 加载指定的yaml文件，解析为map
     *
     * @param path
     * @return
     */
    public static Map<String, Object> loadYaml(String path) {
        Yaml yaml = createYaml();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Iterable<Object> elements = yaml.loadAll(ClassUtils.getContextClassLoader().getResourceAsStream(path));
        Map<String, Object> map = new HashMap<>();
        for (Object o : elements) {
            plain(o, "", map);
        }
        return map;
    }

    /**
     * 解析指定流中的yaml属性值为map
     */
    public static Map<String, Object> loadYaml(InputStream is) {
        Yaml yaml = createYaml();

        Iterable<Object> elements = yaml.loadAll(is);
        Map<String, Object> map = new HashMap<>();
        for (Object o : elements) {
            plain(o, "", map);
        }
        return map;
    }

    /**
     * 创建一个用于解析yaml文件的实例，该实例不是线程安全的
     */
    private static Yaml createYaml() {
        var loaderOptions = new LoaderOptions();
        var dumperOptions = new DumperOptions();
        var representer = new Representer(dumperOptions);
        var resolver = new NoImplicitResolver();
        return new Yaml(new Constructor(loaderOptions), representer, dumperOptions, loaderOptions, resolver);
    }

    /**
     * 将给定树形结构展开，放入map中
     *
     * @param node
     * @param prefix
     * @param map
     */
    private static void plain(Object node, String prefix, Map<String, Object> map) {
        if (node instanceof Map) {
            Set<? extends Map.Entry<?, ?>> entries = ((Map<?, ?>) node).entrySet();
            for (var v : entries) {
                plain(v, prefix, map);
            }
        } else if (node instanceof Map.Entry) {
            Object key = ((Map.Entry<?, ?>) node).getKey();
            Object value = ((Map.Entry<?, ?>) node).getValue();
            if (value instanceof Map) {
                plain(value, prefix + key.toString() + ".", map);
            } else {
                map.put(prefix + key, value);
            }
        } else {
            map.put(prefix, node);
        }
    }

}

/**
 * Disable ALL implicit convert and treat all values as string.
 */
class NoImplicitResolver extends Resolver {

    public NoImplicitResolver() {
        super();
        super.yamlImplicitResolvers.clear();
    }
}
