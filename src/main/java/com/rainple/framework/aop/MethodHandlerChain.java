package com.rainple.framework.aop;/**
 * @Auther: Administrator
 * @Date: 2018/9/24 14:02
 * @PROJECT_NAME webapp
 * @Description:
 */

import java.util.List;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-24 14:02
 **/
public class MethodHandlerChain {

    private List<MethodHandler> handlers;

    private int index = -1;

    public MethodHandlerChain(List<MethodHandler> handlers){
        this.handlers = handlers;
    }

    public void proceed(){
        if (handlers.size() - 1 == index){
            return;
        }
        handlers.get(++index).proceed(this);
    }

}
