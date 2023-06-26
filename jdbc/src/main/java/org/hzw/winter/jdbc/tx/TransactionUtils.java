package org.hzw.winter.jdbc.tx;

import org.hzw.winter.context.util.ApplicationContextUtils;

/**
 * @author hzw
 */
public class TransactionUtils {

    public static TransactionStatus getCurrentTransaction(){
        return ApplicationContextUtils.getRequiredApplicationContext().getBean(TransactionManager.class).getCurrentTransaction();
    }
}
