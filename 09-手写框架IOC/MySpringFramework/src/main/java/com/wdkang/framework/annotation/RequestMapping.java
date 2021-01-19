package com.wdkang.framework.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
  /**
   * 请求路径
   */
  String value() default "";
  
  /**
   * 请求方法
   */
  RequestMethod method() default RequestMethod.GET;
}
