package org.hzw.winter.boot;

import org.apache.catalina.Context;
import org.apache.catalina.Server;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.hzw.winter.context.property.PropertyResolver;
import org.hzw.winter.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @author hzw
 */
public class WinterApplication {
    Logger log = LoggerFactory.getLogger(WinterApplication.class);

    // todo 暂时无法使用此方法，jar包引入的类需要先进行自解压
    public static void run(Class<?> configClass) throws Exception {
        // 判定是否从jar/war启动:
        String jarFile = configClass.getProtectionDomain().getCodeSource().getLocation().getFile();
        boolean isJarFile = jarFile.endsWith(".war") || jarFile.endsWith(".jar");
        // 定位webapp根目录:
        String webDir = isJarFile ? "tmp-webapp" : "src/main/webapp";
        if (isJarFile) {
            // 解压到tmp-webapp:
            Path baseDir = Paths.get(webDir).normalize().toAbsolutePath();
            if (Files.isDirectory(baseDir)) {
                Files.delete(baseDir);
            }
            Files.createDirectories(baseDir);
            System.out.println("extract to: " + baseDir);
            try (JarFile jar = new JarFile(jarFile)) {
                List<JarEntry> entries = jar.stream().sorted(Comparator.comparing(JarEntry::getName)).collect(Collectors.toList());
                for (JarEntry entry : entries) {
                    Path res = baseDir.resolve(entry.getName());
                    if (!entry.isDirectory()) {
                        System.out.println(res);
                        Files.createDirectories(res.getParent());
                        Files.copy(jar.getInputStream(entry), res);
                    }
                }
            }
            // JVM退出时自动删除tmp-webapp:
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.walk(baseDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }
        WinterApplication.run(configClass, webDir, isJarFile ? "tmp-webapp" : "target/classes");
    }

    public static void run(Class<?> configClass, String webDir, String baseDir) throws Exception {
        new WinterApplication().doRun(configClass, WebUtils.createPropertyResolver(), webDir, baseDir);
    }

    /**
     * 启动应用
     */
    public void doRun(Class<?> configClass, PropertyResolver propertyResolver, String webDir, String baseDir) throws Exception {
        Tomcat tomcat = new Tomcat();
        int port = propertyResolver.getProperty("${server.port:8080}", int.class);
        tomcat.setPort(port);

        Connector connector = tomcat.getConnector();
        connector.setThrowOnFailure(true);

        // 添加一个默认的webapp，并将其挂载在根路径 "/" 下
        Context context = tomcat.addWebapp("", new File(webDir).getAbsolutePath());

        WebResourceRoot resourceRoot = new StandardRoot(context);
        resourceRoot.addPreResources(new DirResourceSet(resourceRoot, "/WEB-INF/classes", new File(baseDir).getAbsolutePath(), "/"));
        context.setResources(resourceRoot);

        context.addServletContainerInitializer(new ContextLoaderInitializer(configClass, propertyResolver), Set.of());
        // 启动tomcat
        tomcat.start();
        log.info("application start at port: {}", port);

        Server server = tomcat.getServer();
        // 等待服务停止
        server.await();
    }
}
