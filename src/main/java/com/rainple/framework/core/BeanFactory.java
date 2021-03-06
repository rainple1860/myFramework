package com.rainple.framework.core;

import com.rainple.framework.annotation.Autowired;
import com.rainple.framework.utils.ClassUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class BeanFactory {

    private static final Logger LOGGER = Logger.getLogger(BeanFactory.class);

    private static BeanFactory beanFactory = null;

    private Map<String,Object> ioc = new HashMap<>(16);

    private Map<String,Object> proxyBean = new HashMap<>();

    private BeanFactory(){}

    public static BeanFactory getBeanFactory(){
        if (beanFactory == null){
            synchronized (BeanFactory.class){
                if (beanFactory == null){
                    beanFactory = new BeanFactory();
                }
            }
        }
        return beanFactory;
    }

    public Map<String,Object> getBeans(){
        return ioc;
    }

    public Object putProxyBean(String beanName, Object instance) {
        return proxyBean.put(beanName,instance);
    }

    public Object getProxyBean(String beanName) {
        return proxyBean.get(beanName);
    }

    public Object putBeanForce(String beanName,Object instance) {
        return ioc.put(beanName,instance);
    }

    public Object putBean(String beanName, Object instance){
        if (ioc.containsKey(beanName)) {
            /*Class aClass = ClassUtils.forName(beanName);
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
            }*/
            return ioc.get(beanName);
        }
        return ioc.put(beanName, instance);
    }

    public Object getBean(String beanName){
        return ioc.get(beanName);
    }

    public Object getBean(Class clazz){
        if (clazz.isInterface())
            return getBean(clazz.getName());
        else
            return ioc.get(ClassUtils.lowerFirstCase(clazz.getSimpleName()));
    }

    public void clear(){
        ioc.clear();
    }

    public boolean contains(String beanName) {
        if (ioc.containsKey(beanName)) return true;
        return false;
    }

    public Object createBean(Class beanClass) {

        /**
         * 递归实例依赖的bean，无法解决循环依赖的问题
         */
        for (Field field : beanClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Class<?> fieldType = field.getType();
                Object bean = getBean(fieldType);
                if (bean == null)
                    createBean(fieldType);
            }
        }
        Object newInstance = ClassUtils.newInstance(beanClass);
        putBean(ClassUtils.lowerFirstCase(beanClass.getSimpleName()),newInstance);
        return newInstance;
    }

}
