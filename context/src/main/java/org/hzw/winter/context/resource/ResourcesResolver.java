package org.hzw.winter.context.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * @author hzw
 */
public class ResourcesResolver {
    Logger logger = LoggerFactory.getLogger(ResourcesResolver.class);

    private final String basePackage;

    public ResourcesResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * 扫描指定包下的所有文件, 并通过mapper函数进行处理
     *
     * @param mapper 映射函数, 提供从Resource转换为class name的规则
     * @return class name列表
     */
    public <R> List<R> scan(Function<Resource, R> mapper) throws IOException, URISyntaxException {
        String basePackagePath = this.basePackage.replace(".", "/");
        List<R> list = new ArrayList<>();

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = contextClassLoader.getResources(basePackagePath);
        if (resources == null) {
            return list;
        }

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            URI uri = url.toURI();

            try (Stream<Path> walk = Files.walk(Path.of(uri))) {
                walk.filter(Files::isRegularFile).forEach(p -> {
                    if (p.toString().endsWith(".class")) {
                        String path = replaceSlashToSpot(p.toString());
                        path = path.substring(path.indexOf(this.basePackage));
                        Resource r = new Resource(path);
                        R apply = mapper.apply(r);
                        list.add(apply);
                    }
                });
            }
        }

        return list;
    }

    String replaceSlashToSpot(String str) {
        return str.replace("\\", ".").replace("/", ".");
    }
}
