package org.hzw.winter.jdbc.tx;

import java.sql.Connection;

/**
 * 事务状态
 *
 * @author hzw
 */
public class TransactionStatus {
    final Connection conn;

    public TransactionStatus(Connection conn) {
        this.conn = conn;
    }

    public Connection getConn() {
        return conn;
    }
}
