package com.rainple.framework.core.impl;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 13:13
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.Aspect;
import com.rainple.framework.bean.ComponentBean;
import com.rainple.framework.bean.ComponentBeanFactory;
import com.rainple.framework.core.BeanInstanceHandler;
import com.rainple.framework.utils.ClassUtils;
import org.apache.log4j.Logger;

import java.util.List;

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

    private static final Logger LOGGER = Logger.getLogger(AspectBeanInstanceHandler.class);

    @Override
    protected void handlerProcess(){
        List<ComponentBean> beans = ComponentBeanFactory.getInstance().getBeansByAnnotation(Aspect.class);
        for (ComponentBean bean : beans) {
            Class clazz = bean.getBeanClass();
            if (clazz.isInterface()) continue;
            beanFactory.putBean(ClassUtils.lowerFirstCase(clazz.getSimpleName()),ClassUtils.newInstance(clazz));
        }
    }
}
