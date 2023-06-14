package org.hzw.context.bean.beanpostprocessor;

import org.hzw.context.annotation.Autowired;
import org.hzw.context.annotation.Component;
import org.hzw.context.annotation.Value;
import org.hzw.context.bean.bean1.Cat;

/**
 * @author hzw
 */
@Component
public class OriginBean {
    @Value("${dog.name}")
    private String name;
    private Cat cat;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cat getCat() {
        return cat;
    }

    public void setCat(@Autowired(name = "cat") Cat cat) {
        this.cat = cat;
    }

    public void doSomething() {
        System.out.println("it is a original bean, its name is " + this.name);
    }
}
