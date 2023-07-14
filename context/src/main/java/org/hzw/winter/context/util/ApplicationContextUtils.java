package org.hzw.winter.context.util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.hzw.winter.context.bean.ApplicationContext;

import java.util.Objects;

/**
 * @author hzw
 */
public class ApplicationContextUtils {
    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextUtils.applicationContext = applicationContext;
    }

    @Nonnull
    public static ApplicationContext getRequiredApplicationContext() {
        return Objects.requireNonNull(getApplicationContext(), "application context not set!");
    }

    @Nullable
    private static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
