package com.rainple.framework.core.impl;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 13:07
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.Bean;
import com.rainple.framework.annotation.Configuration;
import com.rainple.framework.core.BeanInstanceHandler;
import com.rainple.framework.utils.ClassUtils;

import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-25 13:07
 **/
public class ConfigurationBeanInstanceHandler extends BeanInstanceHandler {
    @Override
    protected void handlerProcess() {
        for (int i = 0 ; i < beanNames.size() ; i++) {
            try {
                Class clazz = Class.forName(beanNames.get(i));
                if (clazz.isAnnotationPresent(Configuration.class)) {
                    for (Method method : clazz.getMethods()) {
                        if (method.isAnnotationPresent(Bean.class)) {
                            int count = method.getParameterCount();
                            if (count > 0)
                                throw new RuntimeException("注入bean时参数应该为空");
                            Bean beanAnnotation = method.getAnnotation(Bean.class);
                            Object invoke = method.invoke(clazz.newInstance(), null);
                            String name = beanAnnotation.value().trim();
                            if ("".equals(name)) {
                                name = ClassUtils.lowerFirstCase(invoke.getClass().getSimpleName());
                            }
                            beanFactory.putBean(name, invoke);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
