package com.rainple.framework.aop;/**
 * @Auther: Administrator
 * @Date: 2018/9/27 12:53
 * @PROJECT_NAME webapp
 * @Description:
 */

import java.lang.reflect.Method;

/**
 * @program: webapp
 *
 * @description: 前置通知类
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-27 12:53
 **/
public class BeforeAspect extends AbstractAspect{

    protected Integer order;//用于设置执行顺序

    public BeforeAspect(Object adviceInstance,Method adviceMethod, Object[] args,int order) {
        super(adviceInstance, adviceMethod, args);
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
