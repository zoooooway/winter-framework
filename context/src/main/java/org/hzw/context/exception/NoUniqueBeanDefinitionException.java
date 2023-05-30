package org.hzw.context.exception;

/**
 * @author hzw
 */
public class NoUniqueBeanDefinitionException extends BeansException{

    public NoUniqueBeanDefinitionException(String message) {
        super(message);
    }
}
