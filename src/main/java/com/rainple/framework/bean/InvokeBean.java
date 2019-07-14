package com.rainple.framework.bean;

import java.lang.reflect.Method;

/***
 * 用于存放用户请求进来的执行在controller中配置的aop的调用链的信息
 * 因为在调用controller中的方法都是通过反射调用的，所以不需要
 * 为controller创建对应的代理类即可实现aop的功能，
 * 即在metnod.invoke()前后调用即可
 */
public class InvokeBean {

    private Object instance;
    private Method method;
    private Object[] args;
    private int order;

    public static final String BEFORE = "before";
    public static final String AFTER = "after";

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
