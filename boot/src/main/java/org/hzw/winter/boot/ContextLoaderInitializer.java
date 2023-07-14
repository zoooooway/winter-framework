package org.hzw.winter.boot;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import org.hzw.winter.context.bean.AnnotationConfigApplicationContext;
import org.hzw.winter.context.property.PropertyResolver;
import org.hzw.winter.web.WebMvcConfiguration;
import org.hzw.winter.web.servlet.FilterRegistrationBean;
import org.hzw.winter.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author hzw
 */
public class ContextLoaderInitializer implements ServletContainerInitializer {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Class<?> configClass;
    private final PropertyResolver propertyResolver;

    public ContextLoaderInitializer(Class<?> configClass, PropertyResolver propertyResolver) {
        this.configClass = configClass;
        this.propertyResolver = propertyResolver;
    }

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) {
        WebMvcConfiguration.setContext(ctx);

        // 启动IoC容器:
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(this.configClass, this.propertyResolver);

        // 注册Filter
        var filterRegistrationBeans = applicationContext.getBeans(FilterRegistrationBean.class);

        for (FilterRegistrationBean bean : filterRegistrationBeans) {
            List<String> urlPatterns = bean.getUrlPatterns();
            if (urlPatterns == null || urlPatterns.isEmpty()) {
                throw new IllegalArgumentException("No url patterns for {}" + bean.getClass().getName());
            }

            String name = bean.getName();
            Filter filter = Objects.requireNonNull(bean.getFilter(), String.format("%s need provide a not null filter", bean.getClass().getName()));
            var dynamic = ctx.addFilter(name, filter);
            dynamic.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, urlPatterns.toArray(String[]::new));
            log.debug("Register filter: {} success", name);
        }

        // 注册 DispatcherServlet
        WebUtils.registerDispatcherServlet(ctx, applicationContext, propertyResolver);
        log.debug("Register dispatchServlet success");
    }
}
