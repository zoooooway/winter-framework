package org.hzw.winter.jdbc;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    @Nonnull
    public <T> T queryForObject(String sql, Class<T> clazz, Object... args) throws NoSuchMethodException {
        if (String.class == clazz) {
            return (T) queryForObject(sql, StringRowMapper.INSTANCE, args);
        }
        if (Boolean.class == clazz || clazz == boolean.class) {
            return (T) queryForObject(sql, BooleanRowMapper.INSTANCE, args);
        }
        if (Number.class == clazz || clazz.isPrimitive()) {
            return (T) queryForObject(sql, NumberRowMapper.INSTANCE, args);
        }

        return queryForObject(sql, new BeanRowMapper<>(clazz), args);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public <T> List<T> queryForList(String sql, Class<T> clazz, Object... args) throws NoSuchMethodException {
        if (String.class == clazz) {
            return (List<T>) queryForList(sql, StringRowMapper.INSTANCE, args);
        }
        if (Boolean.class == clazz || clazz == boolean.class) {
            return (List<T>) queryForList(sql, BooleanRowMapper.INSTANCE, args);
        }
        if (Number.class == clazz || clazz.isPrimitive()) {
            return (List<T>) queryForList(sql, NumberRowMapper.INSTANCE, args);
        }

        return queryForList(sql, new BeanRowMapper<>(clazz), args);
    }

    @Nullable
    protected <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(preparedStatementCreator(sql, args),
                (PreparedStatement preparedStatement) -> {
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        T t = null;
                        while (resultSet.next()) {
                            if (t == null) {
                                t = rowMapper.mapRow(resultSet, resultSet.getRow());
                            } else {
                                throw new DataAccessException("Multiple rows found");
                            }
                        }

                        return t;
                    }
                });
    }

    @Nonnull
    protected <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(preparedStatementCreator(sql, args),
                (PreparedStatement preparedStatement) -> {
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        List<T> result = new ArrayList<>();
                        while (resultSet.next()) {
                            result.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
                        }

                        return result;
                    }
                });
    }

    public int update(String sql, Object... args) {
        return execute(preparedStatementCreator(sql, args), PreparedStatement::executeUpdate);
    }


    protected <T> T execute(ConnectionCallback<T> action) {
        try (Connection conn = dataSource.getConnection()) {
            return action.doInConnection(conn);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    protected <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) {
        return execute((Connection conn) -> {
            try (PreparedStatement preparedStatement = psc.createPreparedStatement(conn)) {
                return action.doInPreparedStatement(preparedStatement);
            }
        });
    }


    protected PreparedStatementCreator preparedStatementCreator(String sql, Object... args) {
        return (Connection conn) -> {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            bindArgs(preparedStatement, args);
            return preparedStatement;
        };
    }

    private void bindArgs(PreparedStatement statement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
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
