package org.hzw.winter.web.exception;

/**
 * @author hzw
 */
public class MultipleRequestException extends RuntimeException{
    public MultipleRequestException() {
        super();
    }

    public MultipleRequestException(String message) {
        super(message);
    }

    public MultipleRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleRequestException(Throwable cause) {
        super(cause);
    }
}
