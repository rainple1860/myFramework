package com.rainple.framework.core.impl;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 12:45
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.Service;
import com.rainple.framework.bean.ComponentBean;
import com.rainple.framework.bean.ComponentBeanFactory;
import com.rainple.framework.core.BeanInstanceHandler;
import com.rainple.framework.utils.ClassUtils;
import com.rainple.framework.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.util.List;

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
            if (clazz.isInterface()) {//在接口上添加注解
                Object bean = beanFactory.getBean(clazz.getName());
                if (bean != null) return bean;
                if (StringUtils.isEmpty(value))
                    throw new RuntimeException("在接口上添加service注释时必须指定具体的实现类：" + clazz);
                List<Class> assignableFrom = ClassUtils.isAssignableFrom(clazz);
                if (assignableFrom.size() == 0)
                    throw new RuntimeException("找不到接口的实现类：" + clazz);
                if (assignableFrom.size() > 1) {
                    List<ComponentBean> beanList = ComponentBeanFactory.getInstance().getBeansByAnnotation(Service.class);
                    if (beanList.size() == 0) {
                        throw new RuntimeException("找不到接口的实现类：" + clazz);
                    }
                }
                return null;
            }else {
                if ("".equals(value)){
                    //将首字母变小写
                    value = ClassUtils.lowerFirstCase(clazz.getSimpleName());
                }
                Object instance = clazz.newInstance();
                //将接口实例化
                for (Class<?> itf : clazz.getInterfaces()) {
                    beanFactory.putBean(itf.getName(), instance);
                }
                return beanFactory.putBean(value, instance);
            }
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
