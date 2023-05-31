package org.hzw.context.bean.bean1;


import org.hzw.context.annotation.Component;

/**
 * @author hzw
 */
@Component
public class Cat {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Cat{" +
                "name='" + name + '\'' +
                '}';
    }
}
