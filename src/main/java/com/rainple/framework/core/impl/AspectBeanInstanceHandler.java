package com.rainple.framework.core.impl;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 13:13
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.Aspect;
import com.rainple.framework.core.BeanFactory;
import com.rainple.framework.core.BeanInstanceHandler;
import com.rainple.framework.utils.ClassUtils;

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
    protected void handlerProcess(){
        for (String beanName : beanNames) {
            try {
                Class<?> clazz = Class.forName(beanName);
                if (clazz.isAnnotationPresent(Aspect.class)){
                    BeanFactory.getBeanFactory().putBean(ClassUtils.lowerFirstCase(clazz.getSimpleName()),clazz.newInstance());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }
}
