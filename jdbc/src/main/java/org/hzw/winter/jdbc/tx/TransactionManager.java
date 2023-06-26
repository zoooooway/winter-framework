package org.hzw.winter.jdbc.tx;

import jakarta.annotation.Nullable;

/**
 * @author hzw
 */
public interface TransactionManager {

    /**
     * 获取当前事务状态
     */
    @Nullable
    TransactionStatus getCurrentTransaction();
}
