package com.rainple.framework.core;/**
 * @Auther: Administrator
 * @Date: 2018/9/27 12:42
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.aop.AbstractAspect;
import com.rainple.framework.core.filter.HandlerChain;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
 * @create: 2018-09-27 12:42
 **/
public class RegisterCenter {

    public static Map<Method, List<AbstractAspect>> proxyMethodHandlers = new HashMap<>();
    private static Map<Method,List<HandlerChain>> filterMap = new HashMap<>();
    private static Map<String,RequestMappingHandler> handlerMapping = new HashMap<>();

    public static void putToFilter (Method method,HandlerChain handlerChain) {
        List<HandlerChain> handlerChains = filterMap.computeIfAbsent(method, k -> new ArrayList<>());
        handlerChains.add(handlerChain);
    }

    public static List<HandlerChain> getHandlerChains(Method method) {
        return filterMap.get(method);
    }

    public static Map<String,RequestMappingHandler> getHandlerMappings() {
        return handlerMapping;
    }

}
