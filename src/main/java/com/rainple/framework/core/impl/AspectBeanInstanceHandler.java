package com.rainple.framework.core.impl;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 13:13
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.*;
import com.rainple.framework.annotation.aspect.After;
import com.rainple.framework.annotation.aspect.Before;
import com.rainple.framework.aop.advice.AdviceChain;
import com.rainple.framework.aop.advice.AdviceParser;
import com.rainple.framework.aop.advice.AspectInfo;
import com.rainple.framework.core.BeanFactory;
import com.rainple.framework.core.BeanInstanceHandler;
import com.rainple.framework.utils.ClassUtils;
import com.rainple.framework.utils.StringUtils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
        for (Class clazz : beanNames) {
            try {
                if (clazz.isAnnotationPresent(Aspect.class)){
                    Object adviceClass = ClassUtils.newInstance(clazz);
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(Before.class)) {
                            aspectWrapper(clazz,adviceClass,method, method.getAnnotation(Before.class));
                        }else {
                            aspectWrapper(clazz,adviceClass,method, method.getAnnotation(After.class));
                        }
                    }
                    BeanFactory.getBeanFactory().putBean(ClassUtils.lowerFirstCase(clazz.getSimpleName()),clazz.newInstance());
                }
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }
    private void createAspectBean(Class clazz) throws IllegalAccessException {
        Object instance = ClassUtils.newInstance(clazz);
        /**
         * 递归实例依赖的bean，无法解决循环依赖的问题
         */
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Class<?> fieldType = field.getType();
                Object bean;
                if (fieldType.isInterface()) {
                    bean = beanFactory.getBean(fieldType.getName());
                    if (bean == null) {
                        List<Class> assignableFrom = ClassUtils.isAssignableFrom(fieldType);
                        if (assignableFrom == null || assignableFrom.size() == 0) {
                            throw new RuntimeException("找不到实现类:" + fieldType.getName());
                        }
                        if (assignableFrom.size() > 1) {
                            Autowired autowired = field.getAnnotation(Autowired.class);
                            String value = autowired.value().trim();
                            if (StringUtils.isEmpty(value)) {
                                throw new RuntimeException("程序无法知道，注入的是那个实现类：" + fieldType.getName());
                            }
                            bean = beanFactory.getBean(value);
                            if (bean == null) {
                                //有两种可能，第一是还没有对应的实现类还没有被实例化，第二种情况是value没有对应的实现类
                                //所以需要先将实现类加入容器中
                                Class target = null;
                                for (Class cl : assignableFrom) {
                                    boolean match = false;
                                    Annotation[] annotations = cl.getAnnotations();
                                    if (annotations == null || annotations.length == 0)
                                        continue;
                                    for (Annotation annotation : annotations) {
                                        String val = "";
                                        if (annotation instanceof Autowired)
                                            val = ((Autowired) annotation).value();
                                        else if (annotation instanceof Bean)
                                            val = ((Bean) annotation).value();
                                        else if (annotation instanceof Component)
                                            val = ((Component) annotation).value();
                                        else if (annotation instanceof Controller)
                                            val = ((Controller) annotation).value();
                                        else if (annotation instanceof Service)
                                            val = ((Service) annotation).value();
                                        if (val.equals(value)) {
                                            match = true;
                                            break;
                                        }
                                    }
                                    if (match) {
                                        target = cl;
                                        break;
                                    }
                                }
                                if (target == null) {
                                    throw new RuntimeException("程序无法知道，注入的是那个实现类：" + fieldType.getName());
                                }
                                createAspectBean(target);
                            }
                        }else {
                            createAspectBean(assignableFrom.get(0));
                        }
                    }
                }else
                    bean = beanFactory.getBean(fieldType);
                if (bean == null) {
                    if (fieldType.isInterface())
                        bean = beanFactory.getBean(fieldType.getName());
                    //createAspectBean(fieldType);
                }
                field.setAccessible(true);
                field.set(instance,bean);
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Before.class)) {
                aspectWrapper(clazz,instance,method, method.getAnnotation(Before.class));
            }else if (method.isAnnotationPresent(After.class))
                aspectWrapper(clazz,instance,method,method.getAnnotation(After.class));
        }
        if (instance == null && clazz.isInterface()) {
            instance = beanFactory.getBean(clazz.getName());
            beanFactory.putBean(clazz.getName(),instance);
        }else {
            beanFactory.putBean(ClassUtils.lowerFirstCase(clazz.getSimpleName()), instance);
            for (Class aClass : clazz.getInterfaces()) {
                beanFactory.putBean(aClass.getName(),instance);
            }
        }
    }

    private void aspectWrapper(Class clazz, Object adviseInstance, Method method, Annotation aspect) throws IllegalAccessException {
        String expression = "";//被增强方法的表达式
        int order = 0;
        if (aspect instanceof Before) {
            expression = ((Before) aspect).value().trim();
            order = ((Before) aspect).order();
        }else if (aspect instanceof After) {
            expression = ((After) aspect).value().trim();
            order = ((After) aspect).order();
        }
        AdviceParser adviceParser = new AdviceParser(beanNames);
        AdviceChain adviceChain = adviceParser.parse(expression, method,clazz, aspect);
        Class targetClass = adviceChain.getTargetClass();
        if (targetClass.isInterface()) {
            LOGGER.error("增强方法不能为接口",new RuntimeException());
        }
        Object bean = beanFactory.getBean(targetClass);
        if (bean == null) {
            createAspectBean(targetClass);
        }
        Proxy ins = new Proxy(adviceChain);
        Object proxy = ins.getProxy();
        beanFactory.putProxyBean(adviceChain.getTargetClass().getName(),proxy);
    }

    static class Proxy implements MethodInterceptor {

        private AdviceChain adviceChain;
        private Class targetClass;

        public Proxy(AdviceChain adviceChain) {
            this.adviceChain = adviceChain;
            this.targetClass = adviceChain.getTargetClass();
        }

        public Object getProxy() {
            Enhancer enhancer = new Enhancer();
            enhancer.setCallback(this);
            enhancer.setSuperclass(targetClass);
            return enhancer.create();
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            List<AspectInfo> beforeChain = adviceChain.getBeforeChain();
            for (AspectInfo aspectInfo : beforeChain) {
                aspectInfo.getTargetMethod().invoke(aspectInfo.getTargetClass().newInstance(),aspectInfo.getArgs());
            }
            Object invokeSuper = methodProxy.invokeSuper(o, objects);
            List<AspectInfo> afterChain = adviceChain.getAfterChain();
            for (AspectInfo aspectInfo : afterChain) {
                aspectInfo.getTargetMethod().invoke(aspectInfo.getTargetClass().newInstance(),aspectInfo.getArgs());
            }
            return invokeSuper;
        }
    }

}
