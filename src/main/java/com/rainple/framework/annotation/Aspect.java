package com.rainple.framework.annotation;

import java.lang.annotation.*;

/**
 * @Auther: Administrator
 * @Date: 2018/9/24 15:36
 * @PROJECT_NAME webapp
 * @Description:
 */
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Aspect {
}
