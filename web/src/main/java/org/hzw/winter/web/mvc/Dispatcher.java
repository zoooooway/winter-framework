package org.hzw.winter.web.mvc;

import org.hzw.winter.web.mvc.enums.RequestMethod;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * 处理特定url的执行器
 *
 * @author hzw
 */
public class Dispatcher {
    private Pattern urlPattern;
    private Object controller;
    private RequestMethod requestMethod;
    private Method method;
    private Param[] params;
    private boolean isVoid;
    private boolean isRest;
    private boolean isResponseBody;

    public boolean support(String url) {
        return this.urlPattern.matcher(url).matches();
    }

    public Pattern getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(Pattern urlPattern) {
        this.urlPattern = urlPattern;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Param[] getParams() {
        return params;
    }

    public void setParams(Param[] params) {
        this.params = params;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public void setVoid(boolean aVoid) {
        isVoid = aVoid;
    }

    public boolean isRest() {
        return isRest;
    }

    public void setRest(boolean rest) {
        isRest = rest;
    }

    public boolean isResponseBody() {
        return isResponseBody;
    }

    public void setResponseBody(boolean responseBody) {
        isResponseBody = responseBody;
    }
}
