package org.hzw.winter.context.exception;

/**
 * @author hzw
 */
public class BeanCreationException extends BeansException {
    public BeanCreationException(String message) {
        super(message);
    }

    public BeanCreationException(String message, Throwable t) {
        super(message, t);
    }

    public BeanCreationException(Throwable t) {
        super(t);
    }
}
