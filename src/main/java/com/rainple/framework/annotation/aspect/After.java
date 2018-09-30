package com.rainple.framework.annotation.aspect;

import java.lang.annotation.*;

/**
 * @Auther: Administrator
 * @Date: 2018/9/24 15:37
 * @PROJECT_NAME webapp
 * @Description:
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface After {
    String value();
    int order() default 0;
}
