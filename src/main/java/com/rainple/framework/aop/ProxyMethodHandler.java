package com.rainple.framework.aop;/**
 * @Auther: Administrator
 * @Date: 2018/9/24 13:49
 * @PROJECT_NAME webapp
 * @Description:
 */

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-24 13:49
 **/
public class ProxyMethodHandler implements MethodInterceptor {

    private Enhancer enhancer = new Enhancer();
    private Map<Method, List<AbstractAspect>> proxyMethodHandlers;

    public ProxyMethodHandler(Map<Method, List<AbstractAspect>> proxyMethodHandlers){
        this.proxyMethodHandlers = proxyMethodHandlers;
    }
    public Object getProxy(final Object target){
        enhancer.setCallback(this);
        enhancer.setSuperclass(target.getClass());
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        ProxyAdviceProcessor proxyAdviceProcessor = new ProxyAdviceProcessor(o,method,objects,methodProxy,proxyMethodHandlers);
        return proxyAdviceProcessor.invoke();
    }
}
