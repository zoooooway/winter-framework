package org.hzw.context.bean;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.hzw.context.annotation.*;
import org.hzw.context.exception.*;
import org.hzw.context.property.PropertyResolver;
import org.hzw.context.resource.ResourcesResolver;
import org.hzw.context.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通过注解配置的应用程序上下文容器
 *
 * @author hzw
 */
public class AnnotationConfigApplicationContext {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final Map<String, BeanDefinition> beans;
    protected final Set<String> creatingBeanNames;
    protected final PropertyResolver propertyResolver;

    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver) throws IOException, URISyntaxException, ClassNotFoundException {
        this.propertyResolver = propertyResolver;
        // 获取所有类名
        Set<String> classNameSet = scanForClassNames(configClass);

        // 根据类名创建BeanDefinition
        this.beans = createBeanDefinition(classNameSet);

        // 用于检测循环依赖的set
        this.creatingBeanNames = new HashSet<>();
        // 创建bean实例
        createBeanInstance(beans);

        if (log.isDebugEnabled()) {
            this.beans.values().forEach(def -> {
                log.debug("bean initialized: {}", def);
            });
        }

        // 进行依赖注入
        dependencyInjection(beans);

        // 进行调用初始化方法
        initBeans();
    }

    private void initBeans() {
        log.debug("init beans");
        // 调用init方法:
        this.beans.values().forEach(bdf -> {
            try {
                if (bdf.getInitMethod() != null) {
                    bdf.getInitMethod().invoke(bdf.getInstance());

                } else {
                    String initMethodName = bdf.getInitMethodName();
                    if (StringUtils.isEmpty(initMethodName)) {
                        return;
                    }
                    Method method = bdf.getBeanClass().getMethod(initMethodName);
                    method.invoke(bdf.getInstance());
                }

            } catch (Exception e) {
                throw new BeanCreationException(e);
            }
        });
    }

    /**
     * 注入依赖，包括方法和字段注入
     */
    private void dependencyInjection(Map<String, BeanDefinition> beans) {
        beans.forEach((k, v) -> {
            // 依次找出当前类或父类中的方法或字段上存在的autowired注解
            log.debug("inject dependencies for '{}'", k);
            injectMethodAndField(v.getBeanClass(), v.getInstance());
        });
    }

    private void injectMethodAndField(Class<?> clazz, Object instance) {
        try {
            injectField(clazz, instance);
            injectMethod(clazz, instance);
        } catch (Exception e) {
            throw new BeanCreationException(e);
        }

        // 父类属性注入
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            injectMethodAndField(superclass, instance);
        }
    }

    /**
     * 注入属性
     */
    private void injectMethod(Class<?> clazz, Object instance) throws InvocationTargetException, IllegalAccessException {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            // 检查方法参数上是否存在注入的注解
            if (!hasAutowiredOrValueOnParameter(method)) {
                continue;
            }

            // 校验方法是否可符合注入条件
            checkFieldOrMethod(method);

            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Autowired autowired = findAnnotation(parameters[i], Autowired.class);
                Value value = findAnnotation(parameters[i], Value.class);
                if (autowired == null && value == null) {
                    continue;
                }

                if (autowired != null && value != null) {
                    throw new BeanCreationException(String.format("Cannot specify both @Autowired and @Value when inject dependency  %s.", parameters[i].getName()));
                }

                Object property = null;
                if (autowired != null) {
                    // 查找依赖的BeanDefinition，如果指定了依赖的名称，直接使用名称查找，否则按类型查找
                    BeanDefinition beanDefinition;
                    String name = autowired.name();
                    if (StringUtils.isEmpty(name)) {
                        // 先使用参数的名称来进行查找
                        // todo 默认class文件中不存储参数名称，需要使用 -parameters 启动命令来开启存储参数名称， 或者使用字节码框架来进行处理
                        beanDefinition = findBeanDefinition(parameters[i].getName(), parameters[i].getType());
                        if (beanDefinition == null) {
                            // 使用类型查找
                            beanDefinition = findBeanDefinition(parameters[i].getType());
                        }
                    } else {
                        beanDefinition = findBeanDefinition(name, parameters[i].getType());
                    }

                    if (beanDefinition == null) {
                        if (autowired.required()) {
                            throw new UnsatisfiedDependencyException(String.format("Unable to find the required dependency '%s' for %s", parameters[i].getType(), clazz));
                        }
                    }
                    property = beanDefinition.getInstance();

                } else if (value != null) {
                    String key = value.value();
                    Class<?> type = parameters[i].getType();
                    property = propertyResolver.getProperty(key, type);
                }

                args[i] = property;
            }
            injectProperty(instance, method, args);
        }
    }

    private boolean hasAutowiredOrValueOnParameter(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (Annotation[] annos : parameterAnnotations) {
            for (Annotation anno : annos) {
                if (Autowired.class == anno.annotationType() || Value.class == anno.annotationType()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 注入属性
     *
     * @param clazz    需要注入的类
     * @param instance 需要注入的类的实例
     * @throws IllegalAccessException
     */
    private void injectField(Class<?> clazz, Object instance) throws IllegalAccessException, InvocationTargetException {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            Autowired autowired = findAnnotation(field, Autowired.class);
            Value value = findAnnotation(field, Value.class);

            if (check(clazz, field, autowired, value)) {
                continue;
            }

            if (autowired != null) {
                // 查找依赖的BeanDefinition，如果指定了依赖的名称，直接使用名称查找，否则按类型查找
                BeanDefinition beanDefinition;
                String name = autowired.name();
                if (StringUtils.isEmpty(name)) {
                    beanDefinition = findBeanDefinition(field.getType());
                } else {
                    beanDefinition = findBeanDefinition(name, field.getType());
                }

                if (beanDefinition == null) {
                    if (autowired.required()) {
                        throw new UnsatisfiedDependencyException(String.format("Cannot find bean '%s'", field.getType()));
                    }
                } else {
                    injectProperty(instance, field, beanDefinition.getInstance());
                }
            }

            if (value != null) {
                String key = value.value();
                Class<?> type = field.getType();
                Object property = propertyResolver.getProperty(key, type);

                injectProperty(instance, field, property);
            }
        }
    }

    private boolean check(Class<?> clazz, Member member, Autowired autowired, Value value) {
        if (autowired == null && value == null) {
            return true;
        }

        if (autowired != null && value != null) {
            throw new BeanCreationException(String.format("Cannot specify both @Autowired and @Value when inject dependency '%s': %s.", clazz.toString(), member.getName()));
        }

        // 校验
        checkFieldOrMethod(member);
        return false;
    }

    private void injectProperty(Object obj, AccessibleObject accessibleObject, Object property) throws IllegalAccessException, InvocationTargetException {
        if (accessibleObject instanceof Field) {
            Field field = (Field) accessibleObject;
            if (!field.canAccess(obj)) {
                field.setAccessible(true);
            }
            field.set(obj, property);
        } else if (accessibleObject instanceof Method) {
            Method method = (Method) accessibleObject;
            if (!method.canAccess(obj)) {
                method.setAccessible(true);
            }
            method.invoke(obj, (Object[]) property);
        }

    }

    void checkFieldOrMethod(Member m) {
        int mod = m.getModifiers();
        if (Modifier.isStatic(mod)) {
            throw new BeanDefinitionException("Cannot inject static field: " + m);
        }
        if (Modifier.isFinal(mod)) {
            if (m instanceof Field) {
                throw new BeanDefinitionException("Cannot inject final field: " + m);
            }
            if (m instanceof Method) {
                log.warn(
                        "Inject final method should be careful because it is not called on target bean when bean is proxied and may cause NullPointerException.");
            }
        }
    }

    /**
     * 扫描出所有class name
     *
     * @param configClass
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    private Set<String> scanForClassNames(Class<?> configClass) throws IOException, URISyntaxException {
        // 获取扫描路径
        ComponentScan componentScan = configClass.getAnnotation(ComponentScan.class);
        String[] packages = componentScan == null || componentScan.value().length == 0 ? new String[]{configClass.getPackageName()} : componentScan.value();
        if (log.isDebugEnabled()) {
            log.debug("Scan packages: {}", Arrays.toString(packages));
        }

        Set<String> classNameSet = new HashSet<>();
        // 扫描指定路径,找到所有class文件
        for (String pkg : packages) {
            ResourcesResolver resolver = new ResourcesResolver(pkg);
            List<String> scan = resolver.scan(r -> {
                String relativePath = r.getRelativePath();
                return relativePath.substring(0, relativePath.length() - 6);
            });
            classNameSet.addAll(scan);
        }
        if (log.isDebugEnabled()) {
            classNameSet.forEach((className) -> log.debug("class found by component scan: {}", className));
        }
        return classNameSet;
    }

    /**
     * 递归的寻找是否存在指定的注解
     *
     * @param source     被搜寻者
     * @param annotation 搜寻的目标注解
     * @return 搜寻到的注解，未找到则为null
     */
    @Nullable
    private <A extends Annotation> A findAnnotation(AnnotatedElement source, Class<A> annotation) {
        // 是否存在了bean注解
        Annotation[] annotations = source.getAnnotations();
        for (Annotation anno : annotations) {
            // 避免无限递归， 比如@Target注解自身标记自身
            if (anno.annotationType() == source) {
                return null;
            }
            // anno.getClass() 会返回 proxy class 而不是 Annotation的真实 class，
            if (anno.annotationType() == annotation) {
                return source.getAnnotation(annotation);
            }

            A a = findAnnotation(anno.annotationType(), annotation);
            if (a != null) {
                return a;
            }
        }
        return null;
    }

    /**
     * 根据类名创建对应的BeanDefinition
     */
    @Nonnull
    private Map<String, BeanDefinition> createBeanDefinition(Set<String> classNameSet) throws ClassNotFoundException {
        Map<String, BeanDefinition> name2bdf = new HashMap<>();
        // 遍历这些类文件，找到bean
        for (String name : classNameSet) {
            Class<?> clazz = Class.forName(name);
            if (clazz.isAnnotation() || clazz.isEnum() || clazz.isInterface()) {
                continue;
            }

            Component component = findAnnotation(clazz, Component.class);
            if (component != null) {
                int mod = clazz.getModifiers();
                if (Modifier.isAbstract(mod)) {
                    throw new BeanDefinitionException("@Component class " + clazz.getName() + " must not be abstract.");
                }
                if (Modifier.isPrivate(mod)) {
                    throw new BeanDefinitionException("@Component class " + clazz.getName() + " must not be private.");
                }

                BeanDefinition bdf = new BeanDefinition(
                        getBeanName(component, clazz),
                        clazz,
                        null,
                        getSuitableConstructor(clazz),
                        null,
                        null,
                        getOrder(clazz),
                        checkIsPrimary(clazz),
                        null,
                        null,
                        findAnnotationMethod(clazz, PostConstruct.class),
                        findAnnotationMethod(clazz, PreDestroy.class)
                );
                addBeanDefinitions(name2bdf, bdf);

                Configuration configuration = findAnnotation(clazz, Configuration.class);
                if (configuration != null) {
                    // 如果是@Configuration所标记的类，那么需要扫描类中包含的@Bean标记的方法
                    scanFactoryMethod(name2bdf, bdf.getName(), clazz);
                }
            }
        }
        return name2bdf;
    }

    private void scanFactoryMethod(Map<String, BeanDefinition> name2bdf, String factoryBeanName, Class<?> clazz) {
        List<Method> beanMethods = findAnnotationMethods(clazz, Bean.class);
        if (!beanMethods.isEmpty()) {
            for (Method method : beanMethods) {
                Bean bean = method.getAnnotation(Bean.class);

                int mod = method.getModifiers();
                if (Modifier.isAbstract(mod)) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not be abstract.");
                }
                if (Modifier.isFinal(mod)) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not be final.");
                }
                if (Modifier.isPrivate(mod)) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not be private.");
                }
                Class<?> beanClass = method.getReturnType();
                if (beanClass.isPrimitive()) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not return primitive type.");
                }
                if (beanClass == void.class || beanClass == Void.class) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not return void.");
                }

                BeanDefinition bdf = new BeanDefinition(
                        getBeanName(bean, method),
                        method.getReturnType(),
                        null,
                        null,
                        factoryBeanName,
                        method,
                        getOrder(method),
                        checkIsPrimary(method),
                        bean.initMethod(),
                        bean.destroyMethod(),
                        null,
                        null
                );
                addBeanDefinitions(name2bdf, bdf);
            }

        }
    }

    private int getOrder(Class<?> clazz) {
        Order order = findAnnotation(clazz, Order.class);
        if (order == null) {
            return Integer.MAX_VALUE;
        }

        return order.value();
    }

    private int getOrder(Method method) {
        Order order = method.getAnnotation(Order.class);
        if (order == null) {
            return 0;
        }

        return order.value();
    }

    @Nullable
    private Method findAnnotationMethod(Class<?> clazz, Class<? extends Annotation> annotation) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            Annotation anno = m.getAnnotation(annotation);
            if (anno != null) {
                return m;
            }
        }
        return null;
    }

    @Nonnull
    private List<Method> findAnnotationMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Method> list = new ArrayList<>(5);
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            Annotation anno = m.getAnnotation(annotation);
            if (anno != null) {
                list.add(m);
            }
        }
        return list;
    }


    private boolean checkIsPrimary(Class<?> clazz) {
        Primary primary = findAnnotation(clazz, Primary.class);
        return primary != null;
    }

    private boolean checkIsPrimary(Method method) {
        Primary primary = method.getAnnotation(Primary.class);
        return primary != null;
    }

    private String getBeanName(Component component, Class<?> clazz) {
        if (component.name() != null && !"".equals(component.name())) {
            return component.name();
        }

        String name = clazz.getName();
        String className = name.substring(name.lastIndexOf(".") + 1);
        return firstCharToLower(className);
    }

    private String getBeanName(Bean bean, Method method) {
        if (bean.name() != null && !"".equals(bean.name())) {
            return bean.name();
        }

        return firstCharToLower(method.getName());
    }

    private String firstCharToLower(String name) {
        char c = name.charAt(0);
        return Character.toLowerCase(c) + name.substring(1);
    }

    private <T> Constructor<T> getSuitableConstructor(Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        if (constructors.length == 0) {
            Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
            if (declaredConstructors.length != 1) {
                throw new BeanDefinitionException("More than one constructor found in class " + clazz.getName() + ".");
            }
            return (Constructor<T>) declaredConstructors[0];
        } else if (constructors.length == 1) {

            return (Constructor<T>) constructors[0];
        } else {
            throw new BeanDefinitionException("More than one constructor found in class " + clazz.getName() + ".");
        }
    }

    /**
     * Check and add bean definitions.
     */
    void addBeanDefinitions(Map<String, BeanDefinition> name2bdf, BeanDefinition bdf) {
        if (log.isDebugEnabled()) {
            log.debug("add BeanDefinition : {}", bdf.getName());
        }
        if (name2bdf.put(bdf.getName(), bdf) != null) {
            throw new BeanDefinitionException("Duplicate bean name: " + bdf.getName());
        }
    }


    private void createBeanInstance(Map<String, BeanDefinition> bdfs) {
        // 先找出工厂方法的BeanDefinition
        bdfs.values().forEach(bdf -> {
            if (bdf.getBeanClass() != null) {
                if (isConfiguration(bdf)) {
                    // 创建configuration实例
                    createEarlyBeanInstance(bdf);
                }
            }
        });

        // 创建其他普通Bean:
        List<BeanDefinition> normalBeans = beans.values().stream().filter(bdf -> bdf.getInstance() == null).sorted(Comparator.comparing(BeanDefinition::getName)).collect(Collectors.toList());
        normalBeans.forEach(b -> {
            // 也许该bean已经在创建其他bean的时候创建好了
            if (b.getInstance() == null) {
                createEarlyBeanInstance(b);
            }
        });

    }

    private boolean isConfiguration(BeanDefinition bdf) {
        Configuration configuration = this.findAnnotation(bdf.getBeanClass(), Configuration.class);
        return configuration != null;
    }

    /**
     * 创建一个Bean，但不进行字段和方法级别的注入。如果创建的Bean不是Configuration，则在构造方法中注入的依赖Bean会自动创建。
     */
    private void createEarlyBeanInstance(BeanDefinition bdf) {
        log.debug("try to create bean: {}", bdf.getName());
        if (!creatingBeanNames.add(bdf.getName())) {
            // 循环依赖
            throw new UnsatisfiedDependencyException(String.format("Circular dependency detected when create bean '%s'", bdf.getName()));
        }

        // 确定创建bean的方式，构造or工厂方法
        Executable createFunc;
        if (bdf.constructor != null) {
            createFunc = bdf.constructor;
        } else {
            createFunc = bdf.factoryMethod;
        }

        // 尝试创建bean
        Parameter[] parameters = createFunc.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Autowired autowired = parameters[i].getDeclaredAnnotation(Autowired.class);
            Value value = parameters[i].getDeclaredAnnotation(Value.class);

            // 简化：如果当前要创建的bean被@Configuration所修饰，那么禁止其在构造函数中使用@Autowired或者@Autowired注入属性。注入属性只允许字段或Setter以减少复杂度
            if (isConfiguration(bdf) && (autowired != null || value != null)) {
                throw new BeanCreationException(String.format("Using @Autowired or @Value in the constructor of beans marked with @Configuration is not supported '%s': %s.", bdf.getName(), bdf.getBeanClass().getName()));
            }

            if (autowired != null && value != null) {
                // 不允许同时使用@Autowired和@Value
                throw new BeanCreationException(String.format("Cannot specify both @Autowired and @Value when create bean '%s': %s.", bdf.getName(), bdf.getBeanClass().getName()));
            }
            if (value == null && autowired == null) {
                // 不存在注入注解
                throw new BeanCreationException(
                        String.format("Must specify @Autowired or @Value when create bean '%s': %s.", bdf.getName(), bdf.getBeanClass().getName()));
            }

            if (autowired != null) {
                // 注入bean
                String name = autowired.name();

                // 查找依赖的BeanDefinition，如果指定了依赖的名称，直接使用名称查找，否则按类型查找
                BeanDefinition beanDefinition;
                if (StringUtils.isEmpty(name)) {
                    beanDefinition = findBeanDefinition(parameters[i].getType());
                } else {
                    beanDefinition = findBeanDefinition(name, parameters[i].getType());
                }

                if (beanDefinition == null && autowired.required()) {
                    throw new UnsatisfiedDependencyException(String.format("Cannot find bean '%s'", parameters[i].getType()));
                }

                if (beanDefinition == null) {
                    // 没有强制要求注入依赖，默认为空
                    args[i] = null;
                } else {
                    if (beanDefinition.getInstance() == null) {
                        // 递归创建此依赖
                        createEarlyBeanInstance(beanDefinition);
                    }
                    args[i] = beanDefinition.getInstance();
                }

            } else {
                // 注入配置属性
                args[i] = propertyResolver.getProperty(value.value(), parameters[i].getType());
            }
        }

        // 创建bean实例
        if (bdf.getConstructor() != null) {
            // 使用构造方法进行创建
            Constructor<?> constructor = bdf.getConstructor();
            Object obj = null;
            try {
                obj = constructor.newInstance(args);
            } catch (Exception e) {
                throw new BeanCreationException(String.format("Exception when create bean '%s': %s", bdf.getName(), bdf.getBeanClass().getName()), e);
            }
            bdf.setInstance(obj);
        } else {
            // @Bean 标注的实例，需要使用对应的 @Configuration 标注的类实例来调用方法
            String factoryName = bdf.getFactoryName();
            BeanDefinition factoryBdf = beans.get(factoryName);
            Object factory = factoryBdf.getInstance();
            if (factory == null) {
                createEarlyBeanInstance(factoryBdf);
            }
            Method factoryMethod = bdf.getFactoryMethod();
            Object obj = null;
            try {
                obj = factoryMethod.invoke(factory, args);
            } catch (Exception e) {
                throw new BeanCreationException(String.format("Exception when create bean '%s': %s", bdf.getName(), bdf.getBeanClass().getName()), e);
            }
            bdf.setInstance(obj);
        }
    }

    /**
     * 按类型查找BeanDefinition，找不到返回空，找到多个BeanDefinition时返回被@Primary标记的，否则抛出异常
     */
    @Nullable
    private BeanDefinition findBeanDefinition(Class<?> type) {
        List<BeanDefinition> collect = beans.values().stream().filter(b -> type.isAssignableFrom(b.getBeanClass())).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return null;
        }

        if (collect.size() == 1) {
            return collect.get(0);
        }

        List<BeanDefinition> primary = collect.stream().filter(BeanDefinition::isPrimary).collect(Collectors.toList());
        if (primary.size() == 1) {
            return primary.get(0);
        }

        if (primary.isEmpty()) {
            throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, but no @Primary specified.", type.getName()));
        } else {
            throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, and multiple @Primary specified.", type.getName()));
        }
    }

    /**
     * 根据Name和Type查找BeanDefinition，如果Name不存在，返回null，如果Name存在，但Type不匹配，抛出异常。
     */
    @Nullable
    private BeanDefinition findBeanDefinition(String name, Class<?> type) {
        BeanDefinition beanDefinition = beans.get(name);

        if (beanDefinition == null) {
            return null;
        }

        if (!type.isAssignableFrom(beanDefinition.getBeanClass())) {
            throw new BeanNotOfRequiredTypeException(String.format("Autowire required type '%s' but bean '%s' has actual type '%s'.", type.getName(),
                    name, beanDefinition.getBeanClass().getName()));
        }

        return beanDefinition;
    }

    /**
     * 关闭容器
     */
    public void close() {
        log.debug("close container");
        destroyBeans();
    }

    private void destroyBeans() {
        log.debug("destroy beans");
        // 调用destroy方法:
        this.beans.values().forEach(bdf -> {
            try {
                if (bdf.getDestroyMethod() != null) {
                    bdf.getDestroyMethod().invoke(bdf.getInstance());

                } else {
                    String destroyMethodName = bdf.getDestroyMethodName();
                    if (StringUtils.isEmpty(destroyMethodName)) {
                        return;
                    }
                    Method method = bdf.getBeanClass().getMethod(destroyMethodName);
                    method.invoke(bdf.getInstance());
                }

            } catch (Exception e) {
                throw new BeanCreationException(e);
            }
        });
    }

}
