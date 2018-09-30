package com.rainple.framework.aop;

/**
 * @Auther: Administrator
 * @Date: 2018/9/24 13:57
 * @PROJECT_NAME webapp
 * @Description:
 */
public abstract class MethodHandler {

    public void proceed(MethodHandlerChain chain){
        handlerProcess();
        chain.proceed();
    }
    protected abstract void handlerProcess();
}
