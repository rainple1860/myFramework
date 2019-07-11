package com.rainple.framework.bean;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: rainple
 * @create: 2019-07-11 16:36
 **/
public class ComponentBeanFactory {

    private List<ComponentBean> componentBeans = new ArrayList<>();

    private ComponentBeanFactory(){}

    private static class Holder {
        private static final ComponentBeanFactory FACTORY = new ComponentBeanFactory();
    }

    public static ComponentBeanFactory getInstance() {
        return Holder.FACTORY;
    }

    public void addBean(ComponentBean bean) {
        componentBeans.add(bean);
    }

    public void deleteBean(ComponentBean bean) {
        componentBeans.remove(bean);
    }

    public List<ComponentBean> getBeansByAnnotation(Annotation annotation) {
        return componentBeans.stream().filter(bean -> bean.getAnnotation() == annotation).collect(Collectors.toList());
    }

    public List<ComponentBean> getBeans() {
        return componentBeans;
    }

}
