package org.hzw.context.bean;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author hzw
 */
public class AnnotationConfigApplicationContextTest {

    @Test
    public void testScan() throws IOException, URISyntaxException, ClassNotFoundException {
        var context = new AnnotationConfigApplicationContext(ScanApplication.class);
    }
}
