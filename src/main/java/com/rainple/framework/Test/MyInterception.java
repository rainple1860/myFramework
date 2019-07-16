package com.rainple.framework.Test;

import com.rainple.framework.annotation.Component;
import com.rainple.framework.core.filter.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description:
 * @author: rainple
 * @create: 2019-07-16 09:15
 **/
@Component
public class MyInterception implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response) {

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("controller中");
    }

    @Override
    public void afterHandle(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("controller后");
    }
}
