package org.hzw.winter.jdbc.tx;

import jakarta.annotation.Nullable;
import org.hzw.winter.aop.proxy.AroundInvocationHandler;
import org.hzw.winter.context.util.ClassUtils;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 简单的事务管理实现，仅支持REQUIRED传播模式
 *
 * @author hzw
 */
public class SimpleTransactionManager implements TransactionManager, AroundInvocationHandler {
    final DataSource dataSource;

    ThreadLocal<TransactionStatus> tx = new ThreadLocal<>();

    public SimpleTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Object doInvoke(Object proxy, Method method, Object[] args) throws SQLException, IllegalAccessException, InvocationTargetException {
        if (ClassUtils.findAnnotation(method, Transactional.class) != null) {
            return doTransaction(proxy, method, args);

        } else {
            return method.invoke(proxy, args);
        }

    }

    private Object doTransaction(Object proxy, Method method, Object[] args) throws SQLException, IllegalAccessException, InvocationTargetException {
        TransactionStatus ts = tx.get();
        if (ts == null) {
            // 开启新事务
            try (Connection conn = dataSource.getConnection()) {
                // 事务中将忽略自动提交
                boolean autoCommit = conn.getAutoCommit();
                if (autoCommit) {
                    conn.setAutoCommit(false);
                }

                try {
                    tx.set(new TransactionStatus(conn));
                    Object result = method.invoke(proxy, args);
                    conn.commit();
                    return result;
                } catch (InvocationTargetException e) {
                    TransactionException te = new TransactionException(e.getCause());
                    try {
                        // 回滚
                        conn.rollback();
                    } catch (SQLException sqlException) {
                        te.addSuppressed(sqlException);
                    }
                    throw te;
                } finally {
                    tx.remove();
                    if (autoCommit) {
                        // 恢复自动提交
                        conn.setAutoCommit(true);
                    }
                }
            }

        } else {
            // 在已有事务中执行
            return method.invoke(proxy, args);
        }
    }

    @Nullable
    @Override
    public TransactionStatus getCurrentTransaction() {
        return tx.get();
    }
}
