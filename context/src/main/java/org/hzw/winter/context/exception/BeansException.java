package org.hzw.winter.context.exception;

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

    public BeansException(Throwable t) {
        super(t);
    }
}
