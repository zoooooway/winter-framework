package org.hzw.context.bean;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.hzw.context.exception.BeansException;

/**
 * @author hzw
 */
public interface BeanPostProcessor {

    @Nonnull
    default Object postProcessBeforeInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        return bean;
    }

    @Nonnull
    default Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        return bean;
    }

    /**
     * 如果此bean被包装过，则返回其包装前对象
     */
    @Nullable
    default Object postProcessOnSetProperty(@Nonnull Object bean, @Nonnull String beanName) {
        return bean;
    }
}
