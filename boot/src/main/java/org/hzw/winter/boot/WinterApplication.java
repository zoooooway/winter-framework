package org.hzw.winter.boot;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.hzw.winter.context.property.PropertyResolver;
import org.hzw.winter.web.util.WebUtils;

import java.io.File;
import java.util.Set;

/**
 * @author hzw
 */
public class WinterApplication {

    public static void run(Class<?> configClass, String webDir, String baseDir) throws LifecycleException {
        new WinterApplication().doRun(configClass, WebUtils.createPropertyResolver(), webDir, baseDir);
    }

    /**
     * 启动应用
     */
    public void doRun(Class<?> configClass, PropertyResolver propertyResolver, String webDir, String baseDir) throws LifecycleException {
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

        Server server = tomcat.getServer();
        // 等待服务停止
        server.await();
    }
}
