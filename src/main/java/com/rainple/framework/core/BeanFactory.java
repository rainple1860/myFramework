package com.rainple.framework.core;

import com.rainple.framework.annotation.Service;
import com.rainple.framework.utils.ClassUtils;
import com.rainple.framework.utils.StringUtils;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class BeanFactory {

    private static final Logger LOGGER = Logger.getLogger(BeanFactory.class);

    private static BeanFactory beanFactory = null;

    private static Map<String,Object> ioc = new HashMap<>(16);

    private BeanFactory(){}

    public static BeanFactory getBeanFactory(){
        if (beanFactory == null){
            synchronized (BeanFactory.class){
                if (beanFactory == null){
                    return new BeanFactory();
                }
            }
        }
        return beanFactory;
    }

    public Map<String,Object> getBeans(){
        return ioc;
    }

    public Object putBean(String beanName, Object instance){
        if (ioc.containsKey(beanName)) {
            Class aClass = ClassUtils.forName(beanName);
            assert aClass != null;
            if (aClass.isInterface()) {
                if (!aClass.isAnnotationPresent(Service.class))
                    throw new RuntimeException("接口具有多个实现时，需要指定一个实现类的beaName,当前没有指定：" + beanName);
                Service annotation = (Service) aClass.getAnnotation(Service.class);
                String val = annotation.value().trim();
                if (StringUtils.isEmpty(val))
                    throw new RuntimeException("接口具有多个实现时，需要指定一个实现类的beaName,当前没有指定：" + beanName);
                if (ioc.containsKey(val)) {
                    throw new RuntimeException("接口具有多个继承时，需要指定唯一的beanName,已存在beanName："+ val + "," + beanName);
                }
            }else {
                throw new RuntimeException("容器中已存在beanName：" + beanName);
            }
            return ioc.get(beanName);
        }
        return ioc.put(beanName, instance);
    }

    public Object getBean(String beanName){
        return ioc.get(beanName);
    }

    public Object getBean(Class clazz){
        String className = ClassUtils.lowerFirstCase(clazz.getSimpleName());
        return ioc.get(className);
    }

    public void clear(){
        ioc.clear();
    }

}
