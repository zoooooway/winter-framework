package org.hzw.winter.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hzw.winter.context.annotation.Autowired;
import org.hzw.winter.context.annotation.Bean;
import org.hzw.winter.context.annotation.Configuration;
import org.hzw.winter.context.annotation.Value;

import javax.sql.DataSource;

/**
 * 默认的jdbc连接配置
 *
 * @author hzw
 */
@Configuration
public class JdbcConfiguration {

    /**
     *
     * @param driverClassName
     * @param url
     * @param username
     * @param password
     * @param maximumPoolSize
     * @param minimumPoolSize
     * @param connTimeout 如果为0，则会使用Integer.MAX_VALUE
     * @return
     */
    @Bean(destroyMethod = "close")
    public DataSource dataSource(@Value("${winter.datasource.driver-class-name}") String driverClassName,
                                 @Value("${winter.datasource.url}") String url,
                                 @Value("${winter.datasource.username}") String username,
                                 @Value("${winter.datasource.password}") String password,
                                 @Value("${winter.datasource.maximum-pool-size:20}") int maximumPoolSize,
                                 @Value("${winter.datasource.minimum-pool-size:1}") int minimumPoolSize,
                                 @Value("${winter.datasource.connection-timeout:0}") long connTimeout
    ) {
        HikariConfig config = new HikariConfig();
        config.setAutoCommit(false);
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumPoolSize);
        config.setConnectionTimeout(connTimeout);

        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
