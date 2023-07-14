package org.hzw.winter.web.servlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.hzw.winter.context.bean.AnnotationConfigApplicationContext;
import org.hzw.winter.web.WebMvcConfiguration;
import org.hzw.winter.web.exception.ServletErrorException;
import org.hzw.winter.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * servlet监听器，负责初始化或销毁IoC容器
 *
 * @author hzw
 */
public class WinterListener implements ServletContextListener {
    Logger log = LoggerFactory.getLogger(WinterListener.class);


    private Servlet dispatchServlet;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.debug("create dispatch servlet.");
        WebMvcConfiguration.setContext(sce.getServletContext());

        // 初始化IoC容器
        String configuration = sce.getServletContext().getInitParameter("configuration");
        AnnotationConfigApplicationContext applicationContext;
        try {
            applicationContext = new AnnotationConfigApplicationContext(Class.forName(configuration), WebUtils.createPropertyResolver());
        } catch (ClassNotFoundException e) {
            throw new ServletErrorException(e);
        }

        WebUtils.registerDispatcherServlet(sce.getServletContext(), applicationContext, WebUtils.createPropertyResolver());
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 销毁IoC容器
        log.debug("destroy dispatch servlet.");
        dispatchServlet.destroy();
    }
}
