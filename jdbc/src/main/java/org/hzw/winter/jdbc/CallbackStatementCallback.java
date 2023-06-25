package org.hzw.winter.jdbc;

import java.sql.Connection;

/**
 * 指示基于需要预编译语句的操作连接的函数接口
 *
 * @author hzw
 */
@FunctionalInterface
public interface CallbackStatementCallback<T> {
    T doInPreparedStatement(Connection conn);
}
