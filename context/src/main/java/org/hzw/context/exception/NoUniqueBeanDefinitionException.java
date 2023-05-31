package org.hzw.context.exception;

/**
 * @author hzw
 */
public class NoUniqueBeanDefinitionException extends BeansException {

    public NoUniqueBeanDefinitionException(String message) {
        super(message);
    }

    public NoUniqueBeanDefinitionException(String message, Throwable t) {
        super(message, t);
    }

    public NoUniqueBeanDefinitionException(Throwable t) {
        super(t);
    }
}
