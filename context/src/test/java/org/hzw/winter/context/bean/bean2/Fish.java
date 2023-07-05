package org.hzw.winter.context.bean.bean2;

import org.hzw.winter.context.annotation.Component;

/**
 * @author hzw
 */
@Component(value = "bigFish")
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
