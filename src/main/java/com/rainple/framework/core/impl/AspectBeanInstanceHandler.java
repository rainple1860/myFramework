package com.rainple.framework.core.impl;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 13:13
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.Aspect;
import com.rainple.framework.core.BeanInstanceHandler;
import com.rainple.framework.utils.ClassUtils;

import java.lang.annotation.Annotation;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-25 13:13
 **/
public class AspectBeanInstanceHandler extends BeanInstanceHandler {


    @Override
    protected Object handlerProcess(Class clazz){
        if (clazz.isInterface()) return null;
        return beanFactory.putBean(ClassUtils.lowerFirstCase(clazz.getSimpleName()),ClassUtils.newInstance(clazz));
    }

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return Aspect.class;
    }
}
