package org.hzw.winter.web.servlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.hzw.winter.context.bean.AnnotationConfigApplicationContext;
import org.hzw.winter.context.bean.ApplicationContext;
import org.hzw.winter.context.property.PropertyResolver;
import org.hzw.winter.context.util.ClassUtils;
import org.hzw.winter.web.exception.ServletErrorException;
import org.hzw.winter.web.mvc.ViewResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * servlet监听器，负责初始化或销毁IoC容器
 *
 * @author hzw
 */
public class WinterListener implements ServletContextListener {
    Logger log = LoggerFactory.getLogger(WinterListener.class);

    static final String APP_CONFIG_YAML = "/application.yml";
    static final String APP_CONFIG_PROP = "/application.properties";

    private Servlet dispatchServlet;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 初始化IoC容器
        log.debug("create dispatch servlet.");
        String configuration = sce.getServletContext().getInitParameter("configuration");

        ClassLoader classLoader = ClassUtils.getContextClassLoader();
        try {
            URL resource = classLoader.getResource(APP_CONFIG_YAML);
            ApplicationContext context;
            if (resource != null) {
                context = new AnnotationConfigApplicationContext(Class.forName(configuration), PropertyResolver.create(APP_CONFIG_YAML));
            } else {
                context = new AnnotationConfigApplicationContext(Class.forName(configuration), PropertyResolver.create(APP_CONFIG_PROP));
            }

            ViewResolver freeMarkerViewResolver = context.getBean("freeMarkerViewResolver", ViewResolver.class);
            dispatchServlet = new DispatchServlet(context, freeMarkerViewResolver);
            var dynamic = sce.getServletContext().addServlet("dispatchServlet", dispatchServlet);
            dynamic.addMapping("/");
            dynamic.setLoadOnStartup(0);

        } catch (Exception e) {
            throw new ServletErrorException(e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 销毁IoC容器
        log.debug("destroy dispatch servlet.");
        dispatchServlet.destroy();
    }
}
