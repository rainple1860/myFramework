package com.rainple.framework.core;/**
 * @Auther: Administrator
 * @Date: 2018/9/27 12:42
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.aop.AbstractAspect;
import com.rainple.framework.aop.Aspect;
import com.rainple.framework.aop.MethodHandler;

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
 * @create: 2018-09-27 12:42
 **/
public class RegisterCenter {

    public static Map<Method,List<AbstractAspect>> proxyMethodHandlers = new HashMap<>();

}
