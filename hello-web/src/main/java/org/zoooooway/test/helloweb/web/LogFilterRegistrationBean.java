//package org.zoooooway.test.helloweb.web;
//
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import org.hzw.winter.context.annotation.Component;
//import org.hzw.winter.context.annotation.Order;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.List;
//
//@Order(100)
//@Component
//public class LogFilterRegistrationBean extends FilterRegistrationBean {
//
//    @Override
//    public List<String> getUrlPatterns() {
//        return List.of("/*");
//    }
//
//    @Override
//    public Filter getFilter() {
//        return new LogFilter();
//    }
//}
//
//class LogFilter implements Filter {
//
//    final Logger logger = LoggerFactory.getLogger(getClass());
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        HttpServletRequest req = (HttpServletRequest) request;
//        logger.info("{}: {}", req.getMethod(), req.getRequestURI());
//        chain.doFilter(request, response);
//    }
//}