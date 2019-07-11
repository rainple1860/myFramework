package com.rainple.framework.core;/**
 * @Auther: Administrator
 * @Date: 2018/9/25 11:50
 * @PROJECT_NAME webapp
 * @Description:
 */

import java.util.List;

/**
 * @program: webapp
 *
 * @description: 链式调用
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-25 11:50
 **/
public class BeanInstanceHandlerChain {

    private List<BeanInstanceHandler> handlers;
    private List<Class> beanNames;
    private int index = -1;
    public BeanInstanceHandlerChain(List<BeanInstanceHandler> handlers,List<Class> beanNames){
        this.handlers = handlers;
        this.beanNames = beanNames;
    }
    public void proceed(){
        if (handlers.size() - 1 == index){
            return;
        }
        handlers.get(++index).proceed(this,beanNames);
    }
}
