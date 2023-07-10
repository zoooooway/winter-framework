package org.hzw.winter.web.servlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.hzw.winter.web.WebMvcConfiguration;
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
        // 初始化IoC容器
        log.debug("create dispatch servlet.");
        WebMvcConfiguration.setContext(sce.getServletContext());
        String configuration = sce.getServletContext().getInitParameter("configuration");


        this.dispatchServlet = DispatcherServlet.createDispatchServlet(configuration);
        var dynamic = sce.getServletContext().addServlet("dispatchServlet", dispatchServlet);
        dynamic.addMapping("/");
        dynamic.setLoadOnStartup(0);


    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 销毁IoC容器
        log.debug("destroy dispatch servlet.");
        dispatchServlet.destroy();
    }
}
