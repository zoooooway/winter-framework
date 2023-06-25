package org.hzw.winter.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    @SuppressWarnings("unchecked")
    public <T> T queryForObject(String sql, Class<T> clazz, Object... args) throws NoSuchMethodException {
        if (String.class == clazz) {
            return (T) query(sql, StringRowMapper.INSTANCE, args);
        }
        if (Boolean.class == clazz || clazz == boolean.class) {
            return (T) query(sql, BooleanRowMapper.INSTANCE, args);
        }
        if (Number.class == clazz || clazz.isPrimitive()) {
            return (T) query(sql, NumberRowMapper.INSTANCE, args);
        }

        return query(sql, new BeanRowMapper<>(clazz), args);
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(preparedStatementCreator(sql, args),
                (PreparedStatement preparedStatement) -> {
                    boolean execute = preparedStatement.execute();
                    if (execute) {
                        ResultSet resultSet = preparedStatement.getResultSet();
                        return rowMapper.mapRow(resultSet, resultSet.getRow());
                    }

                    return null;
                });
    }


    public <T> T execute(ConnectionCallback<T> action) {
        try (Connection conn = dataSource.getConnection()) {
            return action.doInConnection(conn);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) {
        return execute((Connection conn) -> {
            try (PreparedStatement preparedStatement = psc.createPreparedStatement(conn)) {
                return action.doInPreparedStatement(preparedStatement);
            }
        });
    }


    private PreparedStatementCreator preparedStatementCreator(String sql, Object... args) {
        return (Connection conn) -> {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            bindArgs(preparedStatement, args);
            return preparedStatement;
        };
    }

    private void bindArgs(PreparedStatement statement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i, args[i]);
        }
    }

}

class NumberRowMapper implements RowMapper<Number> {

    static final NumberRowMapper INSTANCE = new NumberRowMapper();

    @Override
    public Number mapRow(ResultSet rs, int rowNum) throws SQLException {
        return (Number) rs.getObject(rowNum);
    }
}

class BooleanRowMapper implements RowMapper<Boolean> {

    static final BooleanRowMapper INSTANCE = new BooleanRowMapper();

    @Override
    public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
        return (Boolean) rs.getObject(rowNum);
    }
}

class StringRowMapper implements RowMapper<String> {

    static final StringRowMapper INSTANCE = new StringRowMapper();

    @Override
    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return (String) rs.getObject(rowNum);
    }
}
