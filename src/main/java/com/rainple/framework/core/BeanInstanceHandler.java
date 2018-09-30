package com.rainple.framework.core;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 11:50
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.Bean;
import com.rainple.framework.aop.MethodHandlerChain;

import java.util.ArrayList;
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

    protected List<String> beanNames;
    protected BeanFactory beanFactory = BeanFactory.getBeanFactory();

    public void proceed(BeanInstanceHandlerChain chain,List<String> beanNames){
        this.beanNames = beanNames;
        handlerProcess();
        chain.proceed();
    }
    protected abstract void handlerProcess();
}
