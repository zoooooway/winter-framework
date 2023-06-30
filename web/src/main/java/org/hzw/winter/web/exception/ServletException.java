package org.hzw.winter.web.exception;

/**
 * 与Servlet相关的异常
 *
 * @author hzw
 */
public class ServletException extends RuntimeException {

    public ServletException() {
    }

    public ServletException(String message) {
        super(message);
    }

    public ServletException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServletException(Throwable cause) {
        super(cause);
    }
}
