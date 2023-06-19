package org.hzw.winter.context.bean;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * 提供给框架内部使用的容器接口
 *
 * @author hzw
 */
public interface ConfigurableApplicationContext extends ApplicationContext {

    @Nonnull
    List<BeanDefinition> findBeanDefinitions(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(String name);

    @Nullable
    BeanDefinition findBeanDefinition(String name, Class<?> requiredType);

    @Nonnull
    Object createBeanAsEarlySingleton(BeanDefinition def);
}
