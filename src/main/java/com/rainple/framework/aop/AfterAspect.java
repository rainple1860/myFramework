package com.rainple.framework.aop;/**
 * @Auther: Administrator
 * @Date: 2018/9/27 17:22
 * @PROJECT_NAME webapp
 * @Description:
 */

import java.lang.reflect.Method;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-27 17:22
 **/
public class AfterAspect extends AbstractAspect{

    public AfterAspect(Object adviceInstance, Method adviceMethod, Object[] args, int order) {
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
