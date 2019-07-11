package com.rainple.framework.aop.advice;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @description: 增强方法的调用链
 * @author: rainple
 * @create: 2019-07-10 11:22
 **/
public class AdviceChain {

    private Method targetMethod;
    private List<AspectInfo> beforeChain = new LinkedList<>();
    private List<AspectInfo> afterChain = new LinkedList<>();
    private Class targetClass;

    public void addBefore(AspectInfo aspectInfo) {
        beforeChain.add(aspectInfo);
    }

    public void addAfter(AspectInfo aspectInfo) {
        afterChain.add(aspectInfo);
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    public List<AspectInfo> getBeforeChain() {
        return beforeChain;
    }

    public List<AspectInfo> getAfterChain() {
        return afterChain;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
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
