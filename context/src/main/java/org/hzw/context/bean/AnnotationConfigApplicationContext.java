package org.hzw.context.bean;

import org.hzw.context.annotation.*;
import org.hzw.context.exception.BeanDefinitionException;
import org.hzw.context.resource.ResourcesResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.*;

/**
 * 通过注解配置的应用程序上下文容器
 *
 * @author hzw
 */
public class AnnotationConfigApplicationContext {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final Map<String, BeanDefinition> beans;

    public AnnotationConfigApplicationContext(Class<?> configClass) throws IOException, URISyntaxException, ClassNotFoundException {
        // 获取所有类名
        Set<String> classNameSet = scanForClassNames(configClass);

        // 根据类名创建BeanDefinition
        this.beans = createBeanDefinition(classNameSet);
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
     * 递归的寻找该类上是否存在指定的注解
     *
     * @param clazz
     * @param annotation
     * @return
     */
    private <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotation) {
        // 是否存在了bean注解
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation anno : annotations) {
            // 避免无限递归， 比如@Target注解自身标记自身
            if (anno.annotationType() == clazz) {
                return null;
            }
            // anno.getClass() 会返回 proxy class 而不是 Annotation的真实 class，
            if (anno.annotationType() == annotation) {
                return clazz.getAnnotation(annotation);
            }

            A a = findAnnotation(anno.annotationType(), annotation);
            if (a != null) {
                return a;
            }
        }
        return null;
    }

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
                    scanFactoryMethod(name2bdf, clazz);
                }
            }
        }
        return name2bdf;
    }

    private void scanFactoryMethod(Map<String, BeanDefinition> name2bdf, Class<?> clazz) {
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
                        method.getName(),
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

}
