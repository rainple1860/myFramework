package com.rainple.framework.core;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 11:50
 * @PROJECT_NAME webapp
 * @Description:
 */

import java.lang.annotation.Annotation;
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
                Object instance = handlerProcess(beanName);
                //todo
            }
        }
        chain.proceed();
    }

    protected abstract Object handlerProcess(Class clazz);

    protected abstract Class<? extends Annotation> getAnnotation();

}
