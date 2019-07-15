package com.rainple.framework.bean;

import com.rainple.framework.annotation.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: rainple
 * @create: 2019-07-11 16:36
 **/
public class ComponentBeanFactory {

    private List<ComponentBean> componentBeans = new ArrayList<>();

    private ComponentBeanFactory(){}

    private static class Holder {
        private static final ComponentBeanFactory FACTORY = new ComponentBeanFactory();
    }

    public static ComponentBeanFactory getInstance() {
        return Holder.FACTORY;
    }

    public void addBean(ComponentBean bean) {
        componentBeans.add(bean);
    }

    public void deleteBean(ComponentBean bean) {
        componentBeans.remove(bean);
    }

    public List<ComponentBean> getBeansByAnnotation(Class<? extends Annotation> annotation) {
        return componentBeans.stream().filter(bean -> bean.getAnnotation().annotationType() == annotation).collect(Collectors.toList());
    }

    public List<ComponentBean> getBeans() {
        return componentBeans;
    }

    public ComponentBean getBeanByBeanName(String beanName) {
        for (ComponentBean componentBean : componentBeans) {
            Annotation beanAnnotation = componentBean.getAnnotation();
            if (beanAnnotation instanceof Service) {
                String val = ((Service) beanAnnotation).value().trim();
                if (beanName.equals(val)) return componentBean;
            }
            else if (beanAnnotation instanceof Controller) {
                String val = ((Controller) beanAnnotation).value().trim();
                if (beanName.equals(val)) return componentBean;
            }
            else if (beanAnnotation instanceof Repository) {
                String val = ((Repository) beanAnnotation).beanName();
                if (beanName.equals(val)) return componentBean;
            }else if (beanAnnotation instanceof Bean) {
                String val = ((Bean) beanAnnotation).value().trim();
                if (beanName.equals(val)) return componentBean;
            }else if (beanAnnotation instanceof Autowired) {
                String val = ((Autowired) beanAnnotation).value().trim();
                if (beanName.equals(val)) return componentBean;
            }else if (beanAnnotation instanceof Component) {
                String val = ((Component) beanAnnotation).value().trim();
                if (beanName.equals(val)) return componentBean;
            }else if (beanAnnotation instanceof Resource) {
                String val = ((Resource) beanAnnotation).value().trim();
                if (beanName.equals(val)) return componentBean;
            }
        }
        return null;
    }

}
