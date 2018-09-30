package com.rainple.framework.aop;/**
 * @Auther: Administrator
 * @Date: 2018/9/24 16:01
 * @PROJECT_NAME webapp
 * @Description:
 */

import java.util.Arrays;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-24 16:01
 **/
public class Advice {

    private String className;
    private String classSimpleName;
    private String methodName;
    private String methodSimpleName;
    private Class[] args;

    private int order;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassSimpleName() {
        return classSimpleName;
    }

    public void setClassSimpleName(String classSimpleName) {
        this.classSimpleName = classSimpleName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodSimpleName() {
        return methodSimpleName;
    }

    public void setMethodSimpleName(String methodSimpleName) {
        this.methodSimpleName = methodSimpleName;
    }

    public Class[] getArgs() {
        return args;
    }

    public void setArgs(Class[] args) {
        this.args = args;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "Advice{" +
                "className='" + className + '\'' +
                ", classSimpleName='" + classSimpleName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodSimpleName='" + methodSimpleName + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
