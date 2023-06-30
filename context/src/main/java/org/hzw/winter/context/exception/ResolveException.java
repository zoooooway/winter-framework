package org.hzw.winter.context.exception;

/**
 * @author hzw
 */
public class ResolveException extends RuntimeException {

    public ResolveException() {
    }

    public ResolveException(String message) {
        super(message);
    }

    public ResolveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResolveException(Throwable cause) {
        super(cause);
    }
}
