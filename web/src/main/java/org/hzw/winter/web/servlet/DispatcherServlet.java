package org.hzw.winter.web.servlet;

import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.hzw.winter.context.bean.AnnotationConfigApplicationContext;
import org.hzw.winter.context.bean.ApplicationContext;
import org.hzw.winter.context.property.PropertyResolver;
import org.hzw.winter.context.util.ClassUtils;
import org.hzw.winter.context.util.StringUtils;
import org.hzw.winter.web.PathUtils;
import org.hzw.winter.web.exception.MultipleRequestException;
import org.hzw.winter.web.exception.ResolveParameterException;
import org.hzw.winter.web.exception.ServletErrorException;
import org.hzw.winter.web.exception.UnknownRequestException;
import org.hzw.winter.web.mvc.Dispatcher;
import org.hzw.winter.web.mvc.ModelAndView;
import org.hzw.winter.web.mvc.Param;
import org.hzw.winter.web.mvc.ViewResolver;
import org.hzw.winter.web.mvc.annotation.*;
import org.hzw.winter.web.mvc.enums.ParamType;
import org.hzw.winter.web.mvc.enums.RequestMethod;
import org.hzw.winter.web.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责请求分派的servlet
 *
 * @author hzw
 */
public class DispatcherServlet extends HttpServlet {
    Logger log = LoggerFactory.getLogger(this.getClass());

    static final String APP_CONFIG_YAML = "application.yml";
    static final String APP_CONFIG_PROP = "application.properties";

    final ApplicationContext context;
    final PropertyResolver propertyResolver;
    ViewResolver viewResolver;
    String resourcePath;
    String faviconPath;

    List<Dispatcher> getDispatchers = new ArrayList<>();
    List<Dispatcher> postDispatches = new ArrayList<>();
    List<Dispatcher> putDispatches = new ArrayList<>();
    List<Dispatcher> delDispatches = new ArrayList<>();

    public DispatcherServlet(ApplicationContext context, PropertyResolver propertyResolver) {
        this.context = context;
        this.propertyResolver = propertyResolver;
    }


    public static DispatcherServlet createDispatchServlet(String configClassName) {
        try {
            ClassLoader classLoader = ClassUtils.getContextClassLoader();
            URL resource = classLoader.getResource(APP_CONFIG_YAML);
            PropertyResolver resolver;
            if (resource != null) {
                resolver = PropertyResolver.create(APP_CONFIG_YAML);
            } else {
                resolver = PropertyResolver.create(APP_CONFIG_PROP);
            }
            return new DispatcherServlet(new AnnotationConfigApplicationContext(Class.forName(configClassName), resolver), resolver);
        } catch (Exception e) {
            throw new ServletErrorException(e);
        }
    }

    @Override
    public void init() throws ServletException {
        this.viewResolver = context.getBean("freeMarkerViewResolver", ViewResolver.class);
        // 初始化静态路径
        this.resourcePath = this.propertyResolver.getProperty("${winter.web.static-path:/static/}", String.class);
        this.faviconPath = this.propertyResolver.getProperty("${winter.web.favicon-path:/favicon.ico}", String.class);

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
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMethod(method);
            dispatcher.setController(bean);

            GetMapping get = ClassUtils.findAnnotation(method, GetMapping.class);
            if (get != null) {
                dispatcher.setRequestMethod(RequestMethod.GET);
                addDispatcher(getDispatchers, dispatcher, method, basePath, get.value());
                continue;
            }

            PostMapping post = ClassUtils.findAnnotation(method, PostMapping.class);
            if (post != null) {
                dispatcher.setRequestMethod(RequestMethod.POST);
                addDispatcher(getDispatchers, dispatcher, method, basePath, post.value());
                continue;
            }

            PutMapping put = ClassUtils.findAnnotation(method, PutMapping.class);
            if (put != null) {
                dispatcher.setRequestMethod(RequestMethod.PUT);
                addDispatcher(getDispatchers, dispatcher, method, basePath, put.value());
                continue;
            }

            DeleteMapping del = ClassUtils.findAnnotation(method, DeleteMapping.class);
            if (del != null) {
                dispatcher.setRequestMethod(RequestMethod.DELETE);
                addDispatcher(getDispatchers, dispatcher, method, basePath, del.value());
            }
        }
    }

    private void addDispatcher(List<Dispatcher> dispatchers, Dispatcher dispatcher, Method method, String... paths) throws ServletException {
        dispatcher.setUrlPattern(getPattern(paths));
        dispatcher.setParams(getParams(method));
        dispatcher.setResponseBody(ClassUtils.findAnnotation(method, ResponseBody.class) != null);
        dispatchers.add(dispatcher);
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
                        String defaultValue = (String) anno.getClass().getMethod("defaultValue").invoke(anno);
                        if (StringUtils.isNotEmpty(defaultValue)) {
                            p.setDefaultValue(defaultValue);
                        }
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
        String uri = req.getRequestURI();
        if (uri.startsWith(this.resourcePath) || uri.startsWith(this.faviconPath)) {
            handleResource(req, resp);
        } else {
            handleService(req, resp, getDispatchers);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleService(req, resp, postDispatches);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleService(req, resp, putDispatches);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleService(req, resp, delDispatches);
    }


    private void handleResource(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String uri = req.getRequestURI();
        ServletContext servletContext = req.getServletContext();

        try (InputStream input = servletContext.getResourceAsStream(uri)) {
            if (input == null) {
                // not found:
                resp.sendError(404, "Not Found");
                return;
            }

            int i = uri.lastIndexOf("/");
            if (i < 0) {
                return;
            }

            String file = uri.substring(i + 1);
            String mimeType = servletContext.getMimeType(file);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            resp.setContentType(mimeType);

            try (ServletOutputStream output = resp.getOutputStream()) {
                input.transferTo(output);
            }

        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    private void handleService(HttpServletRequest req, HttpServletResponse resp, List<Dispatcher> dispatchers) throws IOException, ServletException {
        for (Dispatcher dispatcher : dispatchers) {
            String url = concatPaths(req.getContextPath(), req.getRequestURI());
            if (dispatcher.support(url, RequestMethod.GET)) {
                Object result = doHandle(req, resp, dispatcher, url);
                if (dispatcher.isVoid() || result == null) {
                    return;
                }

                // 将结果写入响应
                if (dispatcher.isRest()) {
                    // 以json格式传输
                    if (resp.isCommitted()) {
                        resp.setContentType("application/json;charset=UTF-8");
                    }

                    if (result instanceof String) {
                        try (PrintWriter writer = resp.getWriter()) {
                            writer.write((String) result);
                            writer.flush();
                        }

                    } else if (result instanceof byte[]) {
                        try (ServletOutputStream os = resp.getOutputStream()) {
                            os.write((byte[]) result);
                            os.flush();
                        }

                    } else {
                        try (PrintWriter writer = resp.getWriter()) {
                            JsonUtils.writeJson(writer, result);
                        }
                    }

                } else {
                    // 以视图形式传输
                    if (resp.isCommitted()) {
                        resp.setContentType("text/html;charset=UTF-8");
                    }

                    // 获取视图解析器
                    if (result instanceof ModelAndView) {
                        String viewName = ((ModelAndView) result).getViewName();
                        viewResolver.render(viewName, ((ModelAndView) result).getModel(), req, resp);

                    } else if (result instanceof String) {
                        if (dispatcher.isResponseBody()) {
                            try (PrintWriter pw = resp.getWriter();) {
                                pw.write((String) result);
                                pw.flush();
                            }

                        } else if (((String) result).startsWith("redirect:")) {
                            // send redirect:
                            resp.sendRedirect(((String) result).substring(9));

                        } else {
                            // 字符串情况下没有@ResponseBody不支持直接返回
                            throw new ServletException("Unable to process String result when handle url: " + url);
                        }

                    } else if (result instanceof byte[]) {
                        if (dispatcher.isResponseBody()) {
                            try (ServletOutputStream os = resp.getOutputStream()) {
                                os.write((byte[]) result);
                                os.flush();
                            }

                        } else {
                            // error:
                            throw new ServletException("Unable to process byte[] result when handle url: " + url);
                        }

                    } else {
                        // error:
                        throw new ServletException(String.format("Unable to process %s result when handle url: %s", result.getClass(), url));
                    }

                }

                return;
            }
        }

        // not found:
        resp.sendError(404, "Not Found");
    }

    private Object doHandle(HttpServletRequest req, HttpServletResponse resp, Dispatcher dispatcher, String url) throws IOException {
        // 执行处理
        Object controller = dispatcher.getController();
        Method method = dispatcher.getMethod();
        log.debug("Processing request by {}.{}()", controller.getClass(), method.getName());
        // 注入参数
        Param[] params = dispatcher.getParams();
        Object[] parameters = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Param param = params[i];
            ParamType paramType = param.getParamType();
            switch (paramType) {
                case PATH_VARIABLE:
                    // 从url路径中获取参数
                    Matcher matcher = dispatcher.getUrlPattern().matcher(url);
                    String group = matcher.group(param.getName());
                    parameters[i] = convertToType(param.getClazz(), group);
                    break;

                case REQUEST_PARAM:
                    Map<String, String[]> parameterMap = req.getParameterMap();
                    String name = param.getName();
                    String[] values = parameterMap.get(name);
                    if (values == null) {
                        if (StringUtils.isNotEmpty(param.getDefaultValue())) {
                            parameters[i] = convertToType(param.getClazz(), param.getDefaultValue());
                        }
                    } else if (values.length == 1) {
                        parameters[i] = convertToType(param.getClazz(), values[0]);
                    } else {
                        throw new ResolveParameterException(String.format("Only support resolve for single parameter but multiple parameters found. method: '%s', param: '%s'", method, param.getClazz()));
                    }

                    break;

                case REQUEST_BODY:
                    Object o = JsonUtils.readJson(req.getReader(), param.getClazz());
                    parameters[i] = o;
                    break;

                case SERVLET_VARIABLE:
                    Class<?> classType = param.getClazz();
                    if (classType == HttpServletRequest.class) {
                        parameters[i] = req;
                    } else if (classType == HttpServletResponse.class) {
                        parameters[i] = resp;
                    } else if (classType == HttpSession.class) {
                        parameters[i] = req.getSession();
                    } else if (classType == ServletContext.class) {
                        parameters[i] = req.getServletContext();
                    } else {
                        throw new ResolveParameterException("Could not determine argument type: " + classType);
                    }
                    break;

                default:
                    throw new UnknownRequestException("Unknown request param type.");
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Resolve parameters: {}", Arrays.toString(parameters));
        }

        try {
            Object invoke = method.invoke(controller, parameters);
            log.debug("Processing {} completed.", url);
            return invoke;
        } catch (Exception e) {
            throw new ServletErrorException(e);
        }
    }


    Object convertToType(Class<?> classType, String s) {
        if (classType == String.class) {
            return s;
        } else if (classType == boolean.class || classType == Boolean.class) {
            return Boolean.valueOf(s);
        } else if (classType == int.class || classType == Integer.class) {
            return Integer.valueOf(s);
        } else if (classType == long.class || classType == Long.class) {
            return Long.valueOf(s);
        } else if (classType == byte.class || classType == Byte.class) {
            return Byte.valueOf(s);
        } else if (classType == short.class || classType == Short.class) {
            return Short.valueOf(s);
        } else if (classType == float.class || classType == Float.class) {
            return Float.valueOf(s);
        } else if (classType == double.class || classType == Double.class) {
            return Double.valueOf(s);
        } else {
            throw new ResolveParameterException("Could not determine argument type: " + classType);
        }
    }

    @Override
    public void destroy() {
        context.close();
        super.destroy();
    }
}

