package org.hzw.winter.context.exception;

/**
 * @author hzw
 */
public class UnsupportedFileException extends ResolveException {

    public UnsupportedFileException() {
    }

    public UnsupportedFileException(String message) {
        super(message);
    }

    public UnsupportedFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedFileException(Throwable cause) {
        super(cause);
    }
}
