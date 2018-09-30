package com.rainple.framework.annotation;

import java.lang.annotation.*;
@Inherited
@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {
    String value();
}
