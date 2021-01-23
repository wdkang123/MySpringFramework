package com.myspring.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {
  /**
   * 包名
   */
  String pkg() default "";
  
  /**
   * 类名
   */
  String cls() default "";
}
