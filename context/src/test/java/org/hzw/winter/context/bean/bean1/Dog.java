package org.hzw.winter.context.bean.bean1;


/**
 * @author hzw
 */
public class Dog extends Pet {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Dog{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }

    public void init() {
        System.out.println("---------------------------init " + name);
    }
    public void destroy() {
        System.out.println("---------------------------destroy " + name);
    }
}
