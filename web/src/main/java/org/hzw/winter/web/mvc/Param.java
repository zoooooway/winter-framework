package org.hzw.winter.web.mvc;

import org.hzw.winter.web.mvc.enums.ParamType;

/**
 * 参数对象
 *
 * @author hzw
 */
public class Param {
    private String name;
    /**
     * 参数的来源类型
     */
    private ParamType paramType;
    /**
     * 参数类型
     */
    private Class<?> clazz;
    /**
     * 默认值
     */
    private String defaultValue;
    /**
     * 是否必须有值
     */
    private boolean isRequired;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public void setParamType(ParamType paramType) {
        this.paramType = paramType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }
}
