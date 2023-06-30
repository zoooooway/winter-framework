package org.hzw.winter.context.resource;

import org.hzw.winter.context.util.ClassUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * @author hzw
 */
public class ResourcesResolver {

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

        ClassLoader contextClassLoader = ClassUtils.getContextClassLoader();
        Enumeration<URL> resources = contextClassLoader.getResources(basePackagePath);
        if (resources == null) {
            return list;
        }

        while (resources.hasMoreElements()) {
            URI uri = resources.nextElement().toURI();

            System.out.println(uri);

            Path path = null;
            FileSystem fileSystem = null;
            if ("file".equals(uri.getScheme())) {
                /*
                file:/H:/hzw/learn/github/winter-framework/aop/target/classes/org/hzw/winter
                 */
                path = Path.of(uri);

            } else if ("jar".equals(uri.getScheme())) {
                /*
                jar:file:/H:/hzw/learn/github/winter-framework/context/target/context-1.0.jar!/org/hzw/winter
                 */
                fileSystem = FileSystems.newFileSystem(uri, Map.of());
                path = fileSystem.getPath(basePackagePath);
            }

            try (Stream<Path> walk = Files.walk(path)) {
                walk.filter(Files::isRegularFile).forEach(p -> {
                    String str = p.toString().replace("\\", "/");
                    int idx = str.indexOf(basePackagePath);

                    Resource r = new Resource(str.substring(0, idx), replaceSlashToSpot(str.substring(idx)));
                    R apply = mapper.apply(r);
                    if (apply != null) {
                        list.add(apply);
                    }

                });
            } finally {
                if (fileSystem != null) {
                    fileSystem.close();
                }
            }
        }

        return list;
    }

    String replaceSlashToSpot(String str) {
        return str.replace("\\", ".").replace("/", ".");
    }
}
