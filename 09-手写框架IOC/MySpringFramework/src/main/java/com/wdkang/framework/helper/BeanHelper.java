package com.wdkang.framework.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.wdkang.framework.util.ReflectionUtil;

public final class BeanHelper {
  /**
   * BEAN_MAP 相当于一个Spring容器
   * 拥有应用所有的Bean的实例
   */
  private static final Map<Class<?>, Object> BEAN_MAP = new HashMap<>();
  
  static {
    // 获取应用中的所有Bean
    Set<Class<?>> beanClassSet = ClassHelper.getBeanClassSet();
    // 将Bean实例化
    for (Class<?> beanClass : beanClassSet) {
      Object obj = ReflectionUtil.newInstance(beanClass);
      BEAN_MAP.put(beanClass, obj);
    }
  }
  
  /**
   * 获取bean容器
   */
  public static Map<Class<?>, Object> getBeanMap() {
    return BEAN_MAP;
  }
  
  /**
   * 获取bean实例
   */
  @SuppressWarnings("unchecked")
  public static <T> T getBean(Class<T> cls) {
    if (BEAN_MAP.containsKey(cls)) {
       throw new RuntimeException("can not get bean by class : " + cls);
    }
    return (T) BEAN_MAP.get(cls);
  }
  
  /**
   * 设置bean实例
   */
  public static void setBean(Class<?> cls, Object obj) {
    BEAN_MAP.put(cls, obj);
  }
  
}
