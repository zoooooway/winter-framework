package org.hzw.winter.web.mvc;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * @author hzw
 */
public interface ViewResolver {

    void init();

    void render(String viewName, Map<String, Object> model, HttpServletRequest req, HttpServletResponse resp) throws IOException;

}
