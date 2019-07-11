package com.rainple.framework.bean;

import java.lang.annotation.Annotation;

/**
 * @description: 容器类
 * @author: rainple
 * @create: 2019-07-11 16:34
 **/
public class ComponentBean {

    private Class beanClass;
    private Annotation annotation;

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }
}
