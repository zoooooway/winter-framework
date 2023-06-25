package org.hzw.winter.jdbc;

import jakarta.annotation.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 指示如何操作连接的函数接口
 *
 * @author hzw
 */
@FunctionalInterface
public interface ConnectionCallback<T> {
    @Nullable
    T doInConnection(Connection conn) throws SQLException;
}
