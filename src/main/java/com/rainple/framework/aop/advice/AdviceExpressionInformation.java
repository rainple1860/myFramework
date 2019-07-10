package com.rainple.framework.aop.advice;

import java.util.List;

/**
 * @description: 增强表达式信息
 * @author: rainple
 * @create: 2019-07-10 11:38
 **/
public class AdviceExpressionInformation {

    private List<String> parameters;
    private String method;
    private String clazz;
    private String pkg;
    private String returnType;
    private String modifier;

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifer(String modifier) {
        this.modifier = modifier;
    }
}
