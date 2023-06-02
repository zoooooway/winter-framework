package org.hzw.context.bean.bean1;

import org.hzw.context.annotation.Value;

/**
 * @author hzw
 */
public class Pet {
    @Value("pet.from")
    private String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "Pet{" +
                "from='" + from + '\'' +
                '}';
    }
}
