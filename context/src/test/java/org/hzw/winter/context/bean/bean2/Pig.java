package org.hzw.winter.context.bean.bean2;

/**
 * @author hzw
 */
public class Pig {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Pig{" +
                "name='" + name + '\'' +
                '}';
    }
}
