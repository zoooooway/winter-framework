package org.hzw.winter.context.util;

/**
 * @author hzw
 */
public class StringUtils {

    public static boolean isEmpty(String s) {
        return s == null || "".equals(s);
    }

    public static boolean isNotEmpty(String s) {
        return s != null && !"".equals(s);
    }
}
