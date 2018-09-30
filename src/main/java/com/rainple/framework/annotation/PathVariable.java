package com.rainple.framework.annotation;

import java.lang.annotation.*;
@Inherited
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PathVariable {
    String value();
}
