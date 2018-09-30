package com.rainple.framework.proxy;/**
 * @Auther: Administrator
 * @Date: 2018/9/24 15:41
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.aop.MethodHandler;
import com.rainple.framework.aop.ProxyMethodHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-24 15:41
 **/
public class ProxyFactory {

    private static ProxyFactory  proxyFactory = null;

    private static Map<Method,List<MethodHandler>> adviceMapping = new HashMap<>();

    private ProxyFactory(){}

    public static ProxyFactory getProxyFactory(){
        if (proxyFactory == null){
            synchronized (ProxyFactory.class){
                if (proxyFactory == null){
                    proxyFactory = new ProxyFactory();
                }
            }
        }
        return proxyFactory;
    }

    public Map<Method,List<MethodHandler>> getAdviceMapping(){
        return adviceMapping;
    }

    public void putAdviceMapping(Method method,List<MethodHandler> proxyMethodHandlers){
        adviceMapping.put(method,proxyMethodHandlers);
    }


}
