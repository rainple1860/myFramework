package com.rainple.framework.core;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 11:50
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.Autowired;
import com.rainple.framework.bean.ComponentBean;
import com.rainple.framework.bean.ComponentBeanFactory;
import com.rainple.framework.utils.ClassUtils;
import com.rainple.framework.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @program: webapp
 *
 * @description: 实例化带有注解的bean
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-25 11:50
 **/
public abstract class BeanInstanceHandler {

    protected BeanFactory beanFactory = BeanFactory.getBeanFactory();

    public void proceed(BeanInstanceHandlerChain chain,List<Class> beanNames){
        for (Class<?> beanName : beanNames) {
            if (beanName.isAnnotationPresent(getAnnotation())) {
                handlerProcess(beanName);
            }
        }
        chain.proceed();
    }

    protected void newInstance(Class clazz) {
        Object instance = ClassUtils.newInstance(clazz);
        try {
            for (Field field : clazz.getDeclaredFields()) {
                Class<?> type = field.getType();
                if (type.isAnnotationPresent(Autowired.class)) {
                    Object bean;
                    if (type.isInterface()) {
                        bean = beanFactory.getBean(clazz.getName());
                        if (bean != null) {
                            field.set(instance, bean);
                            continue;
                        }
                        Autowired autowired = type.getAnnotation(Autowired.class);
                        String value = autowired.value();
                        if (StringUtils.isEmpty(value))
                            throw new RuntimeException("在接口上添加service注释时必须指定具体的实现类：" + clazz);
                        List<Class> assignableFrom = ClassUtils.isAssignableFrom(clazz);
                        if (assignableFrom.size() == 0)
                            throw new RuntimeException("找不到接口的实现类：" + clazz);
                        else if (assignableFrom.size() > 1) {
                            ComponentBean componentBean = ComponentBeanFactory.getInstance().getBeanByBeanName(value);
                            if (componentBean == null) {
                                throw new RuntimeException("找不到beanName为" + value + "接口的实现类：" + clazz);
                            }
                            Class beanClass = componentBean.getBeanClass();
                            Class targetClass = null;
                            for (Class aClass : assignableFrom) {
                                if (aClass == beanClass)
                                    targetClass = aClass;
                            }
                            if (targetClass == null)
                                throw new RuntimeException("找不到接口的实现类：" + clazz);
                            Object bean1 = beanFactory.getBean(targetClass);
                            if (bean1 != null) {
                                field.set(instance, bean1);
                                continue;
                            }
                            newInstance(targetClass);
                        }else {
                            Object bean1 = beanFactory.getBean(assignableFrom.get((0)));
                            if (bean1 != null) {
                                field.set(instance, bean1);
                                continue;
                            }
                            newInstance(assignableFrom.get((0)));
                        }
                    } else {
                        bean = beanFactory.getBean(clazz);
                        if (bean != null) {
                            field.set(instance, bean);
                            continue;
                        }
                        newInstance(type);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected abstract Object handlerProcess(Class clazz);

    protected abstract Class<? extends Annotation> getAnnotation();

}
