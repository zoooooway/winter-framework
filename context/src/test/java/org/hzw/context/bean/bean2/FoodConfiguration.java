package org.hzw.context.bean.bean2;

import org.hzw.context.annotation.Bean;
import org.hzw.context.annotation.Configuration;
import org.hzw.context.annotation.Primary;

/**
 * @author hzw
 */
@Configuration
public class FoodConfiguration {

    @Primary
    @Bean
    public Pig pig1() {
        Pig p = new Pig();
        p.setName("pig1");
        return p;
    }

    @Bean
    public Pig pig2() {
        Pig p = new Pig();
        p.setName("pig2");
        return p;
    }

    @Bean
    public Fish fish() {
        Fish f = new Fish();
        f.setName("fiiiish");
        return f;
    }
}
