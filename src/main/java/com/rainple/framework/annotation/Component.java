package com.rainple.framework.annotation;

import java.lang.annotation.*;

/**
 * @Auther: Administrator
 * @Date: 2018/9/27 11:47
 * @PROJECT_NAME webapp
 * @Description:
 */
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    String value() default "";
}
