package com.wdkang.framework.helper;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import com.wdkang.framework.annotation.Controller;
import com.wdkang.framework.annotation.Service;
import com.wdkang.framework.util.ClassUtil;

public final class ClassHelper {
  /**
   * 定义集合
   */
  private static final Set<Class<?>> CLASS_SET;
  
  static {
    // 获取基础包名
    String basePackage = ConfigHelper.getAppBasePackage();
    CLASS_SET = ClassUtil.getClassSet(basePackage);
  }
  
  /**
   * 获取基础包名下的所有类
   */
  public static Set<Class<?>> getClassSet() {
    return CLASS_SET;
  }
  
  /**
   * 获取基础包名下所有Service类
   */
  public static Set<Class<?>> getServiceClassSet() {
    Set<Class<?>> classSet = new HashSet<>();
    for (Class<?> cls : CLASS_SET) {
      if (cls.isAnnotationPresent(Service.class)) {
        classSet.add(cls);
      }
    }
    return classSet;
  }
  
  /**
   * 获取基础包名下所有Controller类
   */
  public static Set<Class<?>> getControllerClassSet() {
    Set<Class<?>> classSet = new HashSet<>();
    for (Class<?> cls : CLASS_SET) {
      if (cls.isAnnotationPresent(Controller.class)) {
        classSet.add(cls);
      }
    }
    return classSet;
  }
  
  /**
   * 获取基础包名下所有Bean类
   */
  public static Set<Class<?>> getBeanClassSet() {
    Set<Class<?>> beanClassSet = new HashSet<>();
    beanClassSet.addAll(getServiceClassSet());
    beanClassSet.addAll(getControllerClassSet());
    return beanClassSet;
  }
  
  /**
   * 获取基础包名下某父类的所有子类 或 某接口的所有实现类
   */
  public static Set<Class<?>> getClassSetBySuper(Class<?> superClass) {
    Set<Class<?>> classSet = new HashSet<>();
    for (Class<?> cls : CLASS_SET) {
      // isAssignableFrom() 是指superClass 和 cls 是否相同
      // 或 superClass 是否为 cls 的 父类、接口
      if (superClass.isAssignableFrom(cls) && !superClass.equals(cls)) {
        classSet.add(cls);
      }
    }
    return classSet;
  }
  
  /**
   * 获取基础包名下带有某注解的所有类
   */
  public static Set<Class<?>> getClassSetByAnnotation(Class<? extends Annotation> annotationClass) {
    Set<Class<?>> classSet = new HashSet<>();
    for (Class<?> cls : classSet) {
      if (cls.isAnnotationPresent(annotationClass)) {
        classSet.add(cls);
      }
    }
    return classSet;
  }
  
}
