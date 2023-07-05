package org.hzw.winter.web.servlet;

import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hzw.winter.context.bean.ApplicationContext;
import org.hzw.winter.context.util.ClassUtils;
import org.hzw.winter.context.util.StringUtils;
import org.hzw.winter.web.PathUtils;
import org.hzw.winter.web.exception.MultipleRequestException;
import org.hzw.winter.web.exception.UnknownRequestException;
import org.hzw.winter.web.mvc.Dispatcher;
import org.hzw.winter.web.mvc.Param;
import org.hzw.winter.web.mvc.annotation.*;
import org.hzw.winter.web.mvc.enums.ParamType;
import org.hzw.winter.web.mvc.enums.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责请求分派的servlet
 *
 * @author hzw
 */
public class DispatchServlet extends HttpServlet {
    Logger log = LoggerFactory.getLogger(this.getClass());

    final ApplicationContext context;
    List<Dispatcher> getDispatchers = new ArrayList<>();
    List<Dispatcher> postDispatches = new ArrayList<>();
    List<Dispatcher> putDispatches = new ArrayList<>();
    List<Dispatcher> delDispatches = new ArrayList<>();

    public DispatchServlet(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void init() throws ServletException {
        // 找到所有Controller
        List<Object> beans = context.getBeans(Object.class);

        for (Object bean : beans) {
            Controller controller = ClassUtils.findAnnotation(bean.getClass(), Controller.class);
            if (controller != null) {
                RequestMapping requestMapping = ClassUtils.findAnnotation(bean.getClass(), RequestMapping.class);
                String basePath = null;
                if (requestMapping != null) {
                    basePath = requestMapping.value();
                }
                if (StringUtils.isEmpty(basePath)) {
                    basePath = "/";
                }
                // 遍历该bean的方法，找到所有处理url的映射方法
                findMappingMethod(bean, basePath);

            }
        }
    }

    @Nonnull
    private void findMappingMethod(Object bean, String basePath) throws ServletException {
        // 处理一下路径，使其以斜杠开头和结尾，形如 /xx/xx/
//        if (!basePath.startsWith("/")) {
//            basePath = "/" + basePath;
//        }
//        if (!basePath.endsWith("/")) {
//            basePath = basePath + "/";
//        }

        String contextPath = this.getServletContext().getContextPath();
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMethod(method);
            dispatcher.setController(bean);

            GetMapping get = ClassUtils.findAnnotation(method, GetMapping.class);
            if (get != null) {
                dispatcher.setRequestMethod(RequestMethod.GET);
                dispatcher.setUrlPattern(getPattern(contextPath, basePath, get.value()));
                dispatcher.setParams(getParams(method));
                getDispatchers.add(dispatcher);
                continue;
            }

            PostMapping post = ClassUtils.findAnnotation(method, PostMapping.class);
            if (post != null) {
                dispatcher.setRequestMethod(RequestMethod.POST);
                dispatcher.setUrlPattern(getPattern(contextPath, basePath, post.value()));
                dispatcher.setParams(getParams(method));
                postDispatches.add(dispatcher);
                continue;
            }

            PutMapping put = ClassUtils.findAnnotation(method, PutMapping.class);
            if (put != null) {
                dispatcher.setRequestMethod(RequestMethod.PUT);
                dispatcher.setUrlPattern(getPattern(contextPath, basePath, put.value()));
                dispatcher.setParams(getParams(method));
                putDispatches.add(dispatcher);
                continue;
            }

            DeleteMapping del = ClassUtils.findAnnotation(method, DeleteMapping.class);
            if (del != null) {
                dispatcher.setRequestMethod(RequestMethod.DELETE);
                dispatcher.setUrlPattern(getPattern(contextPath, basePath, del.value()));
                dispatcher.setParams(getParams(method));
                delDispatches.add(dispatcher);
            }
        }
    }

    @Nonnull
    private Pattern getPattern(String... paths) throws ServletException {
        String path = concatPaths(paths);

        return PathUtils.compile(path);
    }

    @Nonnull
    private String concatPaths(String... paths) {
        String path = paths[paths.length - 1];

        for (int i = paths.length - 2; i >= 0; i--) {
            if (StringUtils.isNotEmpty(paths[i])) {
                path = concatPath(paths[i], path);
            }
        }
        return path;
    }

    @Nonnull
    private String concatPath(String pre, String suf) {
        if (!pre.startsWith("/")) {
            pre = "/" + pre;
        }
        if (pre.endsWith("/")) {
            pre = pre.substring(0, pre.length() - 1);
        }
        return pre + suf;
    }

    @Nonnull
    private Param[] getParams(Method method) throws ServletException {
        Parameter[] parameters = method.getParameters();
        Param[] params = new Param[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Param p = new Param();
            p.setClazz(parameters[i].getType());
            // 通过注解判断参数的来源
            Annotation[] annotations = parameters[i].getAnnotations();
            ParamType pt = null;
            for (Annotation anno : annotations) {
                if (anno.annotationType() == RequestParam.class) {
                    if (pt != null) {
                        throw new MultipleRequestException(String.format("Found multiple request annotation in method parameter. '%s' : '%s'", method.getName(), parameters[i].getName()));
                    }
                    pt = ParamType.REQUEST_PARAM;
                    try {
                        p.setName((String) anno.getClass().getMethod("value").invoke(anno));
                    } catch (Exception e) {
                        throw new ServletException(e);
                    }
                } else if (anno.annotationType() == RequestBody.class) {
                    if (pt != null) {
                        throw new MultipleRequestException(String.format("Found multiple request annotation in method parameter. '%s' : '%s'", method.getName(), parameters[i].getName()));
                    }
                    pt = ParamType.REQUEST_BODY;
                } else if (anno.annotationType() == PathVariable.class) {
                    if (pt != null) {
                        throw new MultipleRequestException(String.format("Found multiple request annotation in method parameter. '%s' : '%s'", method.getName(), parameters[i].getName()));
                    }
                    pt = ParamType.PATH_VARIABLE;
                    try {
                        p.setName((String) anno.getClass().getMethod("value").invoke(anno));
                    } catch (Exception e) {
                        throw new ServletException(e);
                    }
                }
            }

            if (pt == null) {
                pt = ParamType.SERVLET_VARIABLE;
            }
            p.setParamType(pt);

            params[i] = p;
        }
        return params;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String method = req.getMethod();
        log.info("contextPath: {}, uri: {}, method: {}", contextPath, requestURI, method);
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        for (Dispatcher dispatcher : getDispatchers) {
            String url = concatPaths(req.getContextPath(), req.getRequestURI());
            if (dispatcher.support(url, RequestMethod.GET)) {
                // 执行处理
                Object controller = dispatcher.getController();
                Method method = dispatcher.getMethod();
                // 注入参数
                Param[] params = dispatcher.getParams();
                for (Param param : params) {
                    ParamType paramType = param.getParamType();
                    switch (paramType) {
                        case PATH_VARIABLE:
                            // 从url路径中获取参数
                            Matcher matcher = dispatcher.getUrlPattern().matcher(url);
                            String group = matcher.group();

                            break;
                        case REQUEST_PARAM:
                            break;
                        case REQUEST_BODY:
                            break;
                        case SERVLET_VARIABLE:
                            break;
                        default:
                            throw new UnknownRequestException("Unknown request param type.");
                    }
                }
                try {
                    method.invoke(controller, null);
                } catch (Exception e) {
                    throw new ServletException(e);
                }
            }
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }

    @Override
    public void destroy() {
        context.close();
        super.destroy();
    }
}

