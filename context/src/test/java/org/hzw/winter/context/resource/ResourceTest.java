package org.hzw.winter.context.resource;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;


/**
 * @author hzw
 */
public class ResourceTest {
    Logger logger = LoggerFactory.getLogger(ResourceTest.class);

    @Test
    public void test() throws IOException, URISyntaxException {
        ResourcesResolver rr = new ResourcesResolver("org.hzw");
        List<String> scan = rr.scan(r -> {
            if (r.getName().endsWith(".class")) {
                return r.getName().substring(0, r.getName().length() - 6);
            }
            return null;
        });

        logger.info(scan.toString());
    }
}
