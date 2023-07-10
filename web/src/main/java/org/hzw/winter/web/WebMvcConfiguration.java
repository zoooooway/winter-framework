package org.hzw.winter.web;

import jakarta.servlet.ServletContext;
import org.hzw.winter.context.annotation.Bean;
import org.hzw.winter.context.annotation.Configuration;
import org.hzw.winter.context.annotation.Value;
import org.hzw.winter.web.mvc.FreeMarkerViewResolver;
import org.hzw.winter.web.mvc.ViewResolver;

import java.util.Objects;

/**
 * @author hzw
 */
@Configuration
public class WebMvcConfiguration {
    private static ServletContext context;

    public static void setContext(ServletContext servletContext){
        context = servletContext;
    }

    @Bean
    public ViewResolver freeMarkerViewResolver(@Value("${winter.servlet.templatePath:/static}") String templatePath,
                                     @Value("winter.servlet.templateEncoding:UTF-8") String templateEncoding) {
        return new FreeMarkerViewResolver(context, templatePath, templateEncoding);
    }


    @Bean
    public ServletContext servletContext() {
        return Objects.requireNonNull(context);
    }
}
