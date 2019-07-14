package com.rainple.framework.core.impl;

import com.rainple.framework.annotation.Repository;
import com.rainple.framework.core.BeanInstanceHandler;
import com.rainple.framework.utils.ClassUtils;
import com.rainple.framework.utils.StringUtils;

import java.lang.annotation.Annotation;

public class RepositoryBeanInstanceHandler extends BeanInstanceHandler {
    @Override
    protected Object handlerProcess(Class beanName) {
        Repository rep = (Repository) beanName.getAnnotation(Repository.class);
        String name = rep.beanName().trim();
        Object newInstance = ClassUtils.newInstance(beanName);
        if (StringUtils.isEmpty(name)) {
            return beanFactory.putBean(ClassUtils.lowerFirstCase(beanName.getSimpleName()),newInstance);
        } else {
            return beanFactory.putBean(name,newInstance);
        }
    }

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return Repository.class;
    }
}
