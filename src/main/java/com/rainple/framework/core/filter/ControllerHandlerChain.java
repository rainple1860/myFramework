package com.rainple.framework.core.filter;

import com.rainple.framework.annotation.aspect.Before;
import com.rainple.framework.bean.InvokeBean;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @description:
 * @author: rainple
 * @create: 2019-07-12 16:12
 **/
public class ControllerHandlerChain implements HandlerChain {

    private static final Logger LOGGER = Logger.getLogger(ControllerHandlerChain.class);

    private Class target;
    private Method method;
    private Object[] args;
    private List<InvokeBean> before;
    private List<InvokeBean> after;

    public ControllerHandlerChain(){}

    public ControllerHandlerChain(Class targetClass,Method targetMethod,Object[] args) {
        this.target = targetClass;
        this.method = targetMethod;
        this.args = args;
    }

    @Override
    public Object handle(Object instance,Method method,Object[] args) {
        try {
            if (before != null) {
                for (InvokeBean invokeBean : before) {
                    invokeBean.getMethod().invoke(invokeBean.getInstance(), invokeBean.getArgs());
                }
            }
            Object invoke = method.invoke(instance, args);
            LOGGER.info(method + " 方法开始执行");
            if (after != null) {
                for (InvokeBean invokeBean : after) {
                    invokeBean.getMethod().invoke(invokeBean.getInstance(), invokeBean.getArgs());
                }
            }
            return invoke;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 之所以每次添加链时都要排一次降序，主要考虑到
     * 用户发请求调用调用链的时候会根据设置的order属性来优先执行
     * 如果每次调用前进行一次排序，运行时效率比较低，所以选择在
     * 容器创建时就排好序，运行时效率相对会高一点
     * @param invokeBean 调用链的方法信息
     * @param adviseType 前置或后置通知
     */
    public void putChain(InvokeBean invokeBean,Class adviseType) {
        if (adviseType == Before.class)
            putBefore(invokeBean);
        else
            putAfter(invokeBean);
    }

    private void putBefore(InvokeBean invokeBean) {
        if (before == null)
            before = new LinkedList<>();
        before.add(invokeBean);
        before.sort((o1, o2) -> o2.getOrder() - o1.getOrder());
    }

    private void putAfter(InvokeBean invokeBean) {
        if (after == null)
            after = new ArrayList<>();
        after.add(invokeBean);
        after.sort((o1, o2) -> o2.getOrder() - o1.getOrder());
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Class target) {
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

}
