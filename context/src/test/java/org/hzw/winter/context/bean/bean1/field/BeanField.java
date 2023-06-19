package org.hzw.winter.context.bean.bean1.field;

import org.hzw.winter.context.annotation.Autowired;
import org.hzw.winter.context.annotation.Component;
import org.hzw.winter.context.annotation.Value;
import org.hzw.winter.context.bean.bean1.setter.Bean1;

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
