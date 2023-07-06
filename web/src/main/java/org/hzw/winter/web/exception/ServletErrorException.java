package org.hzw.winter.web.exception;

/**
 * 与Servlet相关的异常
 *
 * @author hzw
 */
public class ServletErrorException extends RuntimeException {

    public ServletErrorException() {
    }

    public ServletErrorException(String message) {
        super(message);
    }

    public ServletErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServletErrorException(Throwable cause) {
        super(cause);
    }
}
