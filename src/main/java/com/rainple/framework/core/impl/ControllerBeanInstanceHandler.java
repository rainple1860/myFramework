package com.rainple.framework.core.impl;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 12:29
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.Controller;
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
 * @create: 2018-09-25 12:29
 **/
public class ControllerBeanInstanceHandler extends BeanInstanceHandler {

    @Override
    protected Object handlerProcess(Class clazz) {
        try {
            //Class<?> clazz = Class.forName(beanName);
            if (clazz.isAnnotationPresent(Controller.class)){
                Controller controller = (Controller) clazz.getAnnotation(Controller.class);
                String value = controller.value();
                if ("".equals(value)){
                    value = ClassUtils.lowerFirstCase(clazz.getSimpleName());
                }
                return beanFactory.putBean(value,clazz.newInstance());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return Controller.class;
    }
}
