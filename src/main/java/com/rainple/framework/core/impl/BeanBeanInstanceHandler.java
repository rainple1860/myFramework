package com.rainple.framework.core.impl;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 13:10
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.Bean;
import com.rainple.framework.annotation.Value;
import com.rainple.framework.core.ApplicationConfig;
import com.rainple.framework.core.BeanFactory;
import com.rainple.framework.core.BeanInstanceHandler;
import com.rainple.framework.utils.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-25 13:10
 **/
public class BeanBeanInstanceHandler extends BeanInstanceHandler {
    @Override
    protected void handlerProcess() {
        BeanFactory beanFactory = BeanFactory.getBeanFactory();
        for (Class<?> clazz : super.beanNames) {
            try {
                //Class<?> clazz = Class.forName(beanName);
                if (clazz.isAnnotationPresent(Bean.class)){//直接在pojo类中进行赋值
                    Bean beanAnno = clazz.getAnnotation(Bean.class);
                    String bv = beanAnno.value().trim();
                    Field[] fields = clazz.getDeclaredFields();
                    Object newInstance = clazz.newInstance();
                    //先从字段开始赋值
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(Value.class)){
                            String injectVal = field.getAnnotation(Value.class).value().trim();
                            if ("".equals(injectVal))
                                throw new RuntimeException("bean注入失败：" + newInstance);
                            //从配置文件获取值
                            String value = ApplicationConfig.applicationConfig.getProperty(injectVal);
                            field.setAccessible(true);
                            ClassUtils.setField(newInstance,field,value);
                        }
                    }
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(Value.class)){
                            String methodName = method.getName();
                            if (!methodName.startsWith("set"))
                                continue;
                            Value value = method.getAnnotation(Value.class);
                            String key = value.value().trim();
                            //获取配置文件的值
                            String v = ApplicationConfig.applicationConfig.getProperty(key);
                            ClassUtils.setSetmethod(newInstance,method,v);
                        }
                    }
                    if ("".equals(bv)){
                        bv = ClassUtils.lowerFirstCase(clazz.getSimpleName());
                    }
                    beanFactory.putBean(bv,newInstance);
                }
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }
}
