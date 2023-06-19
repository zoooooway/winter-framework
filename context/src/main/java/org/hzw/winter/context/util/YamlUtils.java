package org.hzw.winter.context.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author hzw
 */
public class YamlUtils {

    /**
     * 加载指定的yaml文件，解析为map
     * @param path
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public Map<String, Object> loadYaml(String path) throws IOException, URISyntaxException {
        var loaderOptions = new LoaderOptions();
        var dumperOptions = new DumperOptions();
        var representer = new Representer(dumperOptions);
        var resolver = new NoImplicitResolver();
        var yaml = new Yaml(new Constructor(loaderOptions), representer, dumperOptions, loaderOptions, resolver);
        URL resources = getContextClassLoader().getResource(path);
        if (resources == null) {
            throw new NoSuchFileException(path);
        }

        Iterable<Object> elements = yaml.loadAll(Files.newInputStream(Path.of(resources.toURI())));
        Map<String, Object> map = new HashMap<>();
        for (Object o : elements) {
            plain(o, "", map);
        }
        return map;
    }

    /**
     * 将给定树形结构展开，放入map中
     *
     * @param node
     * @param prefix
     * @param map
     */
    private void plain(Object node, String prefix, Map<String, Object> map) {
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

    static ClassLoader getContextClassLoader() {
        ClassLoader cl = null;
        cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = YamlUtils.class.getClassLoader();
        }
        return cl;
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

}
