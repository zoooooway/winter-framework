package org.hzw.context.exception;

/**
 * @author hzw
 */
public class UnsatisfiedDependencyException extends BeansException {
    public UnsatisfiedDependencyException(String message) {
        super(message);
    }
}
