package org.hzw.winter.web.exception;

/**
 * @author hzw
 */
public class ResolveParameterException extends BadRequestException {
    public ResolveParameterException() {
        super();
    }

    public ResolveParameterException(String message) {
        super(message);
    }

    public ResolveParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResolveParameterException(Throwable cause) {
        super(cause);
    }
}
