package com.rainple.framework.aop;/**
 * @Auther: Administrator
 * @Date: 2018/9/27 12:54
 * @PROJECT_NAME webapp
 * @Description:
 */

import java.lang.reflect.Method;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-27 12:54
 **/
public abstract class AbstractAspect implements Aspect {

    protected Method adviceMethod; //增强方法
    protected Object adviceInstance;//增强方法的实例
    protected Object[] args;//增强方法的参数列表
    protected  int order;


    public AbstractAspect(Object adviceInstance,Method adviceMethod, Object[] args) {
        this.adviceMethod = adviceMethod;
        this.adviceInstance = adviceInstance;
        this.args = args;
    }

    public AbstractAspect() {
    }

    public Method getAdviceMethod() {
        return adviceMethod;
    }

    public void setAdviceMethod(Method adviceMethod) {
        this.adviceMethod = adviceMethod;
    }

    public Object getAdviceInstance() {
        return adviceInstance;
    }

    public void setAdviceInstance(Object adviceInstance) {
        this.adviceInstance = adviceInstance;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

}
