package com.rainple.framework.core.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: rainple
 * @create: 2019-07-15 17:48
 **/
public class HandlerInterceptorSupporter {

    public static List<HandlerInterceptor> handlerInterceptors = new ArrayList<>();
    private HttpServletRequest request;
    private HttpServletResponse response;

    public HandlerInterceptorSupporter(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public boolean preHandle() {
        for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
            boolean preHandle = handlerInterceptor.preHandle(request, response);
            if (!preHandle) return false;
        }
        return true;
    }
    public void postHandle() {
        for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
            handlerInterceptor.postHandle(request, response);
        }
    }
    public void afterHandle() {
        for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
            handlerInterceptor.afterHandle(request, response);
        }
    }
}
