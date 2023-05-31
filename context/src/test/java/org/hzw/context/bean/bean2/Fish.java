package org.hzw.context.bean.bean2;

import org.hzw.context.annotation.Component;

/**
 * @author hzw
 */
@Component(name = "bigFish")
public class Fish {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Fish{" +
                "name='" + name + '\'' +
                '}';
    }
}
