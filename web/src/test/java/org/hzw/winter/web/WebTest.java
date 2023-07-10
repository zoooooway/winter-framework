package org.hzw.winter.web;

import jakarta.servlet.ServletException;
import org.hzw.winter.context.bean.AnnotationConfigApplicationContext;
import org.hzw.winter.context.property.PropertyResolver;
import org.hzw.winter.web.servlet.DispatcherServlet;
import org.hzw.winter.web.util.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * @author hzw
 */
public class WebTest {

    DispatcherServlet dispatcherServlet;
    MockServletContext ctx;


    @Test
    public void tetGet() throws ServletException, IOException {

        var mockRequest1 = createMockRequest("GET", "/friendly/greeting", null, Map.of("name", "ash"));
        var mockResponse1 = createMockResponse();

        try {
            dispatcherServlet.service(mockRequest1, mockResponse1);
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof ServletException);
        }

        var mockRequest2 = createMockRequest("GET", "/friendly/farewell", null, Map.of("name", "ash"));
        var mockResponse2 = createMockResponse();
        dispatcherServlet.service(mockRequest2, mockResponse2);
        Assertions.assertEquals(200, mockResponse2.getStatus());
        Assertions.assertEquals("goodbye! ash", mockResponse2.getContentAsString());


        var mockRequest3 = createMockRequest("GET", "/friendly/farewell", null, null);
        var mockResponse3 = createMockResponse();
        dispatcherServlet.service(mockRequest3, mockResponse3);
        Assertions.assertEquals(200, mockResponse3.getStatus());
        Assertions.assertEquals("goodbye! sir", mockResponse3.getContentAsString());
    }


    @BeforeEach
    void init() throws ServletException, IOException, URISyntaxException, ClassNotFoundException {
        this.ctx = createMockServletContext();
        WebMvcConfiguration.setContext(this.ctx);
        var propertyResolver = createPropertyResolver();
        var applicationContext = new AnnotationConfigApplicationContext(WebMvcConfiguration.class, propertyResolver);
        this.dispatcherServlet = new DispatcherServlet(applicationContext, propertyResolver);
        this.dispatcherServlet.init();
    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        ps.put("app.title", "Scan App");
        ps.put("app.version", "v1.0");
        ps.put("summer.web.favicon-path", "/icon/favicon.ico");
        ps.put("summer.web.freemarker.template-path", "/WEB-INF/templates");
        ps.put("jdbc.username", "sa");
        ps.put("jdbc.password", "");
        var pr = new PropertyResolver(ps);
        return pr;
    }

    MockServletContext createMockServletContext() {
        Path path = Path.of("./src/test/resources").toAbsolutePath().normalize();
        var ctx = new MockServletContext("file://" + path.toString());
        ctx.setRequestCharacterEncoding("UTF-8");
        ctx.setResponseCharacterEncoding("UTF-8");
        return ctx;
    }

    MockHttpServletRequest createMockRequest(String method, String path, Object body, Map<String, String> params) {
        var req = new MockHttpServletRequest(this.ctx, method, path);
        if (method.equals("GET") && params != null) {
            params.keySet().forEach(key -> {
                req.setParameter(key, params.get(key));
            });
        } else if (method.equals("POST")) {
            if (body != null) {
                req.setContentType("application/json");
                req.setContent(JsonUtils.writeJson(body).getBytes(StandardCharsets.UTF_8));
            } else {
                req.setContentType("application/x-www-form-urlencoded");
                if (params != null) {
                    params.keySet().forEach(key -> {
                        req.setParameter(key, params.get(key));
                    });
                }
            }
        }
        var session = new MockHttpSession();
        req.setSession(session);
        return req;
    }

    MockHttpServletResponse createMockResponse() {
        var resp = new MockHttpServletResponse();
        resp.setDefaultCharacterEncoding("UTF-8");
        return resp;
    }
}
