package org.hzw.context.exception;

/**
 * @author hzw
 */
public class BeanNotOfRequiredTypeException extends BeansException {
    public BeanNotOfRequiredTypeException(String message) {
        super(message);
    }

    public BeanNotOfRequiredTypeException(String message, Throwable t) {
        super(message, t);
    }
}
