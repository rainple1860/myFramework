package com.rainple.framework.annotation;

import java.lang.annotation.*;
@Inherited
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {
    String value() default "";
}
