package org.hzw.winter.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 简单的jdbc的模板类
 *
 * @author hzw
 */
public class JdbcTemplate {
    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(ConnectionCallback<T> action)  {
        try (Connection conn = dataSource.getConnection()) {
            return action.doInConnection(conn);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

}
