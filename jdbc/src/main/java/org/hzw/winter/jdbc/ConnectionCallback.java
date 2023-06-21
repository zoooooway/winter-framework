package org.hzw.winter.jdbc;

import jakarta.annotation.Nullable;

import java.sql.Connection;

/**
 * 指示如何操作连接的标记接口
 *
 * @author hzw
 */
@FunctionalInterface
public interface ConnectionCallback<T> {
    @Nullable
    T doInConnection(Connection conn);
}
