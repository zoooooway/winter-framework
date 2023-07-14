package org.hzw.winter.web.servlet;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.Filter;

import java.util.List;

/**
 * 支持以Bean的方式注册Servlet Filter
 * @author hzw
 */
public interface FilterRegistrationBean {
    @Nullable
    List<String> getUrlPatterns();

    /**
     * Get name by class name. Example:
     * <p>
     * ApiFilterRegistrationBean -> apiFilter
     * <p>
     * ApiFilterRegistration -> apiFilter
     * <p>
     * ApiFilterReg -> apiFilterReg
     */
    @Nonnull
    default String getName() {
        String name = getClass().getSimpleName();
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        if (name.endsWith("FilterRegistrationBean") && name.length() > "FilterRegistrationBean".length()) {
            return name.substring(0, name.length() - "FilterRegistrationBean".length());
        }
        if (name.endsWith("FilterRegistration") && name.length() > "FilterRegistration".length()) {
            return name.substring(0, name.length() - "FilterRegistration".length());
        }
        return name;
    }

    @Nonnull
    Filter getFilter();
}
