package com.rainple.framework.core;

import com.rainple.framework.utils.ClassUtils;

import java.util.HashMap;
import java.util.Map;

public class BeanFactory {

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
            throw new RuntimeException("bean注入失败，容器中已有实例：" + beanName);
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
