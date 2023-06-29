package org.hzw.winter.context.bean.bean1;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.hzw.winter.context.annotation.Component;
import org.hzw.winter.context.annotation.Value;


/**
 * @author hzw
 */
@Component
public class Cat extends Pet {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(@Value("${cat.name}") String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Cat{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }

    @PostConstruct
    public void init() {
        System.out.println("---------------------------init " + getClass().toString());
    }

    @PreDestroy
    public void destroy() {
        System.out.println("---------------------------destroy " + getClass().toString());
    }
}
