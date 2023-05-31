package org.hzw.context.bean.bean1.field;

import org.hzw.context.annotation.Autowired;
import org.hzw.context.annotation.Component;
import org.hzw.context.annotation.Value;
import org.hzw.context.bean.bean1.setter.Bean1;

/**
 * @author hzw
 */
@Component
public class BeanField {
    @Value("${teacher.name}")
    private String name;
    @Autowired
    private Bean1 bean1;
}
