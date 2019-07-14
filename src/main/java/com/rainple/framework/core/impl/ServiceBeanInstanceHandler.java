package com.rainple.framework.core.impl;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 12:45
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.Service;
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
 * @create: 2018-09-25 12:45
 **/
public class ServiceBeanInstanceHandler extends BeanInstanceHandler {
    @Override
    protected Object handlerProcess(Class clazz) {
        try {
            Service service = (Service) clazz.getAnnotation(Service.class);
            String value = service.value();
            if ("".equals(value)){
                //将首字母变小写
                value = ClassUtils.lowerFirstCase(clazz.getSimpleName());
            }
            Object instance = clazz.newInstance();
            //将接口实例化
            for (Class<?> itf : clazz.getInterfaces()) {
                beanFactory.putBean(itf.getName(),instance);
            }
            return beanFactory.putBean(value,instance);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return Service.class;
    }
}
