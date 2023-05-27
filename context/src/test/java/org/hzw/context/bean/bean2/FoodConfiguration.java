package org.hzw.context.bean.bean2;

import org.hzw.context.annotation.Bean;
import org.hzw.context.annotation.Configuration;

/**
 * @author hzw
 */
@Configuration
public class FoodConfiguration {

    @Bean
    public Pig pig() {
        Pig p = new Pig();
        p.setName("pipig");
        return p;
    }

    @Bean
    public Fish fish() {
        Fish f = new Fish();
        f.setName("fiiiish");
        return f;
    }
}
