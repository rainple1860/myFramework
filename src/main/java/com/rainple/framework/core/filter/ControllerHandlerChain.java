package com.rainple.framework.core.filter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @description:
 * @author: rainple
 * @create: 2019-07-12 16:12
 **/
public class ControllerHandlerChain implements HandlerChain {

    private Object target;
    private Method method;
    private Object[] args;
    private int order;

    public ControllerHandlerChain(){}

    public ControllerHandlerChain(Object targetInstance,Method targetMethod,Object[] args,int order) {
        this.target = targetInstance;
        this.method = targetMethod;
        this.args = args;
        this.order = order;
    }

    @Override
    public void handle() {
        try {
            method.invoke(target,args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
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
