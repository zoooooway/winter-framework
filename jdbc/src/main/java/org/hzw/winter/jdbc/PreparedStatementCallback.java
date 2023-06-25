package org.hzw.winter.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author hzw
 */
@FunctionalInterface
public interface PreparedStatementCallback<T> {
    T doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException;
}
