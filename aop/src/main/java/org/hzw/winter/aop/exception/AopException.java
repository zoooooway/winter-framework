package org.hzw.winter.aop.exception;

/**
 * @author hzw
 */
public class AopException extends RuntimeException {
    public AopException() {
    }

    public AopException(String message) {
        super(message);
    }

    public AopException(String message, Throwable cause) {
        super(message, cause);
    }

    public AopException(Throwable cause) {
        super(cause);
    }
}
