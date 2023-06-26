package org.hzw.winter.jdbc.tx;

/**
 * 指示事务执行过程中发生异常
 *
 * @author hzw
 */
public class TransactionException extends RuntimeException {
    public TransactionException() {
        super();
    }

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionException(Throwable cause) {
        super(cause);
    }
}
