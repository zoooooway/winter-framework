package org.hzw.winter.context.exception;

/**
 * @author hzw
 */
public class BeanDefinitionException extends BeansException {
    public BeanDefinitionException(String message) {
        super(message);
    }

    public BeanDefinitionException(String message, Throwable t) {
        super(message, t);
    }

    public BeanDefinitionException(Throwable t) {
        super(t);
    }
}
