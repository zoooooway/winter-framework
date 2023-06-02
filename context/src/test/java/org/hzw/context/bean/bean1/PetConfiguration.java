package org.hzw.context.bean.bean1;

import org.hzw.context.annotation.Bean;
import org.hzw.context.annotation.Configuration;

/**
 * @author hzw
 */
@Configuration
public class PetConfiguration {

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public Dog dog() {
        Dog dog = new Dog();
        dog.setName("hash");
        return dog;
    }

    @Bean
    public Cat redCat() {
        Cat cat = new Cat();
        cat.setName("red");
        return cat;
    }

    @Bean
    public Cat buleCat() {
        Cat cat = new Cat();
        cat.setName("buleCat");
        return cat;
    }
}
