package com.rainple.framework.Test;/**
 * @Auther: Administrator
 * @Date: 2018/9/24 15:45
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.annotation.Aspect;
import com.rainple.framework.annotation.Component;
import com.rainple.framework.annotation.aspect.After;
import com.rainple.framework.annotation.aspect.Before;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-24 15:45
 **/
@Component
@Aspect
public class AspectTest {

    @Before(value = "* com.rainple.framework.service.impl.UserServiceImpl.add(..)",order = 20)
    public void testAspect(){
        System.out.println("在目标方法之前调用20");
    }

    @Before(value = "* com.rainple.framework.service.impl.UserServiceImpl.add(..)",order = 30)
    public void testAspect1(){
        System.out.println("在目标方法之前调用30");
    }

    @Before(value = "* com.rainple.framework.service.impl.UserServiceImpl.*(..)",order = 4)
    public void testAspect3(){
        System.out.println("在目标方法之前调用4");
    }

    @After("* com.rainple.framework.service.impl.UserServiceImpl.add(..)")
    public void testAspect2(){
        System.out.println("在目标方法之后调用");
    }

}
