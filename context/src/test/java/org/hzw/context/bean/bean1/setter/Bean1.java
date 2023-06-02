package org.hzw.context.bean.bean1.setter;

import org.hzw.context.annotation.Autowired;
import org.hzw.context.annotation.Component;
import org.hzw.context.annotation.Value;
import org.hzw.context.bean.bean1.Cat;

/**
 * @author hzw
 */
@Component
public class Bean1 {
    private String name;
    private Cat cat;

    public void setName(@Value("${student.name}") String name, @Autowired(name = "cat") Cat cat) {
        this.name = name;
        this.cat = cat;
    }

    @Override
    public String toString() {
        return "Bean1{" +
                "name='" + name + '\'' +
                ", cat=" + cat +
                '}';
    }
}
