package org.zoooooway.test.helloweb;

import org.hzw.winter.context.annotation.ComponentScan;
import org.hzw.winter.context.annotation.Configuration;
import org.hzw.winter.context.annotation.Import;
import org.hzw.winter.jdbc.JdbcConfiguration;
import org.hzw.winter.web.WebMvcConfiguration;

/**
 * @author hzw
 */
@ComponentScan
@Configuration
@Import({JdbcConfiguration.class, WebMvcConfiguration.class})
public class HelloWebConfiguration {
}
