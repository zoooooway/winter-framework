package org.hzw.winter.context.bean.bean2;

import org.hzw.winter.context.annotation.Bean;
import org.hzw.winter.context.annotation.Configuration;
import org.hzw.winter.context.annotation.Primary;

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
