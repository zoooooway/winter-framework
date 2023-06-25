package org.hzw.winter.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author hzw
 */
@FunctionalInterface
public interface PreparedStatementCreator {
    /**
     * 创建预编译语句
     */
    PreparedStatement createPreparedStatement(Connection conn) throws SQLException;
}
