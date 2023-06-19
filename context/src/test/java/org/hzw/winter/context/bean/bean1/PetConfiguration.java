package org.hzw.winter.context.bean.bean1;

import org.hzw.winter.context.annotation.Bean;
import org.hzw.winter.context.annotation.Configuration;

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
