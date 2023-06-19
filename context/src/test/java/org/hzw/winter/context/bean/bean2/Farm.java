package org.hzw.winter.context.bean.bean2;

import org.hzw.winter.context.annotation.Autowired;
import org.hzw.winter.context.annotation.Component;

/**
 * @author hzw
 */
@Component
public class Farm {
    private final Pig pig1;
    private final Pig pig2;

    public Farm(@Autowired(name = "pig1") Pig pig1, @Autowired(name = "pig2") Pig pig2) {
        this.pig1 = pig1;
        this.pig2 = pig2;
    }

    @Override
    public String toString() {
        return "Farm{" +
                "pig1=" + pig1 +
                ", pig2=" + pig2 +
                '}';
    }
}
