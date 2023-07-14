package org.hzw.winter.web.util;

import jakarta.servlet.ServletContext;
import org.hzw.winter.context.bean.ApplicationContext;
import org.hzw.winter.context.property.PropertyResolver;
import org.hzw.winter.context.util.ApplicationContextUtils;
import org.hzw.winter.context.util.ClassUtils;
import org.hzw.winter.web.exception.ServletErrorException;
import org.hzw.winter.web.servlet.DispatcherServlet;

import java.net.URL;

/**
 * @author hzw
 */
public class WebUtils {

    static final String APP_CONFIG_YAML = "application.yml";
    static final String APP_CONFIG_PROP = "application.properties";

    public static PropertyResolver createPropertyResolver() {
        try {
            ClassLoader classLoader = ClassUtils.getContextClassLoader();
            URL resource = classLoader.getResource(APP_CONFIG_YAML);
            if (resource != null) {
                return PropertyResolver.create(APP_CONFIG_YAML);
            } else {
                return PropertyResolver.create(APP_CONFIG_PROP);
            }
        } catch (Exception e) {
            throw new ServletErrorException(e);
        }
    }

    public static void registerDispatcherServlet(ServletContext servletContext, PropertyResolver propertyResolver) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet(ApplicationContextUtils.getRequiredApplicationContext(), propertyResolver);
        var dynamic = servletContext.addServlet("dispatchServlet", dispatcherServlet);
        dynamic.addMapping("/");
        dynamic.setLoadOnStartup(0);
    }

    public static void registerDispatcherServlet(ServletContext servletContext, ApplicationContext context, PropertyResolver propertyResolver) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet(context, propertyResolver);
        ApplicationContextUtils.setApplicationContext(context);
        var dynamic = servletContext.addServlet("dispatchServlet", dispatcherServlet);
        dynamic.addMapping("/");
        dynamic.setLoadOnStartup(0);
    }
}
