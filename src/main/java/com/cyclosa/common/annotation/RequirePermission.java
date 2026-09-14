package com.cyclosa.common.annotation;

import com.cyclosa.common.enums.DataScope;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    String value() default "";

    String code() default "";

    DataScope minScope() default DataScope.OWN;
}
