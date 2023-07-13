package org.hzw.winter.web.exception;

/**
 * @author hzw
 */
public class UnknownRequestException extends BadRequestException {
    public UnknownRequestException() {
        super();
    }

    public UnknownRequestException(String message) {
        super(message);
    }

    public UnknownRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownRequestException(Throwable cause) {
        super(cause);
    }
}
