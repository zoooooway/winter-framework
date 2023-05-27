package org.hzw.context.bean.bean1;

import org.hzw.context.annotation.Bean;
import org.hzw.context.annotation.Configuration;

/**
 * @author hzw
 */
@Configuration
public class PetConfiguration {

    @Bean
    public Dog dog() {
        Dog dog = new Dog();
        dog.setName("hash");
        return dog;
    }
}
