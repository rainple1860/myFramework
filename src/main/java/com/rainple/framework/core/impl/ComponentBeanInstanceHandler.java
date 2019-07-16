package com.rainple.framework.core.impl;

import com.rainple.framework.annotation.Component;
import com.rainple.framework.core.BeanInstanceHandler;
import com.rainple.framework.core.filter.HandlerInterceptor;
import com.rainple.framework.core.filter.HandlerInterceptorSupporter;
import com.rainple.framework.utils.ClassUtils;

import java.lang.annotation.Annotation;

/**
 * @description: 处理容器注释的类
 * @author: rainple
 * @create: 2019-07-15 17:43
 **/
public class ComponentBeanInstanceHandler extends BeanInstanceHandler {
    @Override
    protected Object handlerProcess(Class clazz) {
        if (HandlerInterceptor.class.isAssignableFrom(clazz)) {
            Object instance = ClassUtils.newInstance(clazz);
            HandlerInterceptorSupporter.handlerInterceptors.add((HandlerInterceptor) instance);
            return instance;
        }
        return null;
    }

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return Component.class;
    }
}
