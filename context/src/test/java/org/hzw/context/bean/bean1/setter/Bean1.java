package org.hzw.context.bean.bean1.setter;

import org.hzw.context.annotation.Component;
import org.hzw.context.annotation.Value;

/**
 * @author hzw
 */
@Component
public class Bean1 {
    private String name;

    @Value("${student.name}")
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Bean1{" +
                "name='" + name + '\'' +
                '}';
    }
}
