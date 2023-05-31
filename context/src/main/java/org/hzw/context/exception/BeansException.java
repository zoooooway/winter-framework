package org.hzw.context.exception;

/**
 * @author hzw
 */
public class BeansException extends RuntimeException {
    public BeansException(String message) {
        super(message);
    }

    public BeansException(String message, Throwable t) {
        super(message, t);
    }
}
