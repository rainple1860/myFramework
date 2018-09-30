package com.rainple.framework.aop;/**
 * @Auther: Administrator
 * @Date: 2018/9/27 16:59
 * @PROJECT_NAME webapp
 * @Description:
 */

import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @program: webapp
 *
 * @description: 执行切面方法
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-27 16:59
 **/
public abstract class AbstractAdviceProcessor {

    public final Object invoke(){
        doBefore();
        Object o = invokeTarget();
        doAfter();
        return o;
    }
    protected abstract Object invokeTarget();
    protected abstract void doBefore();
    protected abstract void doAfter();
}
