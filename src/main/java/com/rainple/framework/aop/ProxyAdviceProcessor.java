package com.rainple.framework.aop;

import com.rainple.framework.core.RegisterCenter;
import com.rainple.framework.utils.ListUtil;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-27 17:13
 **/
public class ProxyAdviceProcessor extends AbstractAdviceProcessor {

    private Object target;
    private Method method;
    private Object[] args;
    private MethodProxy methodProxy;

    public ProxyAdviceProcessor(Object target, Method method, Object[] args, MethodProxy methodProxy) {
        this.target = target;
        this.method = method;
        this.args = args;
        this.methodProxy = methodProxy;
    }

    @Override
    protected Object invokeTarget() {
        try {
            return methodProxy.invokeSuper(target,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    @Override
    protected void doBefore() {
        List<AbstractAspect> abstractAspects = RegisterCenter.proxyMethodHandlers.get(method);
        if (abstractAspects == null)
            return;
        List<AbstractAspect> orderList = sortOfBefore(abstractAspects);
        if (orderList == null)
            return;
        for (AbstractAspect abstractAspect : orderList) {
            if (abstractAspect instanceof BeforeAspect){
                Object adviceInstance = abstractAspect.getAdviceInstance();
                Method adviceMethod = abstractAspect.getAdviceMethod();
                Object[] args = abstractAspect.getArgs();
                try {
                    adviceMethod.invoke(adviceInstance,args);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<AbstractAspect> sortOfBefore(List<AbstractAspect> abstractAspects) {
        List<AbstractAspect> list = new ArrayList<>();
        if (abstractAspects == null)
            return null;
        for (AbstractAspect abstractAspect : abstractAspects) {
            if (abstractAspect instanceof BeforeAspect){
                list.add(abstractAspect);
            }
        }
        return ListUtil.sort(list,"order",ListUtil.DESC,0,list.size());
    }

    private List<AbstractAspect> sortOfAfter(List<AbstractAspect> abstractAspects, String order, String desc) {
        List<AbstractAspect> list = new ArrayList<>();
        for (AbstractAspect abstractAspect : abstractAspects) {
            if (abstractAspect instanceof AfterAspect){
                list.add(abstractAspect);
            }
        }
        return ListUtil.sort(list,"order",ListUtil.DESC,0,list.size());
    }

    @Override
    protected void doAfter() {
        List<AbstractAspect> abstractAspects = RegisterCenter.proxyMethodHandlers.get(method);
        if (abstractAspects == null)
            return;
        List<AbstractAspect> sortOfAfter = sortOfAfter(abstractAspects,"order",ListUtil.DESC);
        for (AbstractAspect abstractAspect : sortOfAfter) {
            if (abstractAspect instanceof AfterAspect){
                Object adviceInstance = abstractAspect.getAdviceInstance();
                Method adviceMethod = abstractAspect.getAdviceMethod();
                Object[] args = abstractAspect.getArgs();
                try {
                    adviceMethod.invoke(adviceInstance,args);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
