package com.rainple.framework.aop.advice;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @description:
 * @author: rainple
 * @create: 2019-07-11 14:55
 **/
public class AspectInfo {

    private Class targetClass;
    private Method targetMethod;
    private Object[] args;

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "AspectInfo{" +
                "targetClass=" + targetClass +
                ", targetMethod=" + targetMethod +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
