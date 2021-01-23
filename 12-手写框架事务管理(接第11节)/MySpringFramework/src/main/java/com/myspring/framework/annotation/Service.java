package com.myspring.framework.annotation;

import java.lang.annotation.*;

/**
 * 服务类注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
}
