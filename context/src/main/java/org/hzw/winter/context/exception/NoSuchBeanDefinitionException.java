package org.hzw.winter.context.exception;

/**
 * @author hzw
 */
public class NoSuchBeanDefinitionException extends BeansException {
    public NoSuchBeanDefinitionException(String message) {
        super(message);
    }

    public NoSuchBeanDefinitionException(String message, Throwable t) {
        super(message, t);
    }

    public NoSuchBeanDefinitionException(Throwable t) {
        super(t);
    }
}
