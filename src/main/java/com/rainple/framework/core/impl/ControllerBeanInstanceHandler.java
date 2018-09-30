package com.rainple.framework.core.impl;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 12:29
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.Controller;
import com.rainple.framework.core.BeanInstanceHandler;
import com.rainple.framework.utils.ClassUtils;

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
    protected void handlerProcess() {
        for (String beanName : beanNames) {
            try {
                Class<?> clazz = Class.forName(beanName);
                if (clazz.isAnnotationPresent(Controller.class)){
                    Controller controller = clazz.getAnnotation(Controller.class);
                    String value = controller.value();
                    if ("".equals(value)){
                        value = ClassUtils.lowerFirstCase(clazz.getSimpleName());
                    }
                    beanFactory.putBean(value,clazz.newInstance());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
