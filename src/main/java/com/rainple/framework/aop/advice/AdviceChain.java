package com.rainple.framework.aop.advice;

import javax.tools.Diagnostic;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @description: 增强方法的调用链
 * @author: rainple
 * @create: 2019-07-10 11:22
 **/
public class AdviceChain {

    private Method targetMethod;
    private List<Method> beforeChain = new LinkedList<>();
    private List<Method> afterChain = new LinkedList<>();

    public static final String BEFORE = "before";
    public static final String AFTER = "after";

    public void addBefore(Method method) {
        beforeChain.add(method);
    }

    public void addAfter(Method method) {
        afterChain.add(method);
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    public List<Method> getBeforeChain() {
        return beforeChain;
    }

    public List<Method> getAfterChain() {
        return afterChain;
    }

    @Override
    public String toString() {
        return "AdviceChain{" +
                "targetMethod=" + targetMethod +
                ", beforeChain=" + beforeChain +
                ", afterChain=" + afterChain +
                '}';
    }
}
