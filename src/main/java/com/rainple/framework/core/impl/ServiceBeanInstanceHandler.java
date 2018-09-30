package com.rainple.framework.core.impl;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 12:45
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.Service;
import com.rainple.framework.core.BeanInstanceHandler;
import com.rainple.framework.utils.ClassUtils;

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
    protected void handlerProcess() {
        for (String beanName : beanNames) {
            try {
                Class<?> clazz = Class.forName(beanName);
                if (clazz.isAnnotationPresent(Service.class)){
                    Service service = clazz.getAnnotation(Service.class);
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
                    beanFactory.putBean(value,instance);
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
