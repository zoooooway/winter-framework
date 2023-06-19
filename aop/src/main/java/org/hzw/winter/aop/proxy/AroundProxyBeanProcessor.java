package org.hzw.winter.aop.proxy;

import org.hzw.winter.aop.annotation.Around;
import org.hzw.winter.context.annotation.Component;

/**
 * 实现@Around代理的BeanPostProcessor
 *
 * @author hzw
 */
@Component
public class AroundProxyBeanProcessor extends ProxyBeanProcessor<Around> {

}
