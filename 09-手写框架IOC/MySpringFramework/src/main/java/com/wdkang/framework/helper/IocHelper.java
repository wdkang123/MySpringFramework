package com.wdkang.framework.helper;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.wdkang.framework.annotation.Autowired;
import com.wdkang.framework.util.ReflectionUtil;

public final class IocHelper {
  /**
   * 遍历bean容器所有bean属性
   * 为所有带@Autowired注解的属性注入实例
   */
  static {
    // 遍历bean容器里的所有bean
    Map<Class<?>, Object> beanMap = BeanHelper.getBeanMap();
    if (MapUtils.isNotEmpty(beanMap)) {
      for (Map.Entry<Class<?>, Object> beanEntry : beanMap.entrySet()) {
        // bean的class类
        Class<?> beanClass = beanEntry.getKey();
        // bean的实例
        Object beanInstance = beanEntry.getValue();
        // 暴力反射 获得属性
        Field[] beanFields = beanClass.getDeclaredFields();
        // 遍历bean的属性
        if (ArrayUtils.isNotEmpty(beanFields)) {
          for (Field beanField : beanFields) {
            // 判断属性是否带有@Autowired
            if (beanField.isAnnotationPresent(Autowired.class)) {
              // 属性类型
              Class<?> beanFieldClass = beanField.getType();
              // 如果beanFieldClass的接口 就获取接口对应的实现类
              Object beanFieldInstance = beanMap.get(beanFieldClass);
              if (beanFieldInstance != null) {
                ReflectionUtil.setField(beanInstance, beanField, beanFieldInstance);
              }
            }
          }
        }
      }
    }
  }
  
  /**
   * 获取接口对应的实现类
   */
  public static Class<?> findImplementClass(Class<?> interfaceClass) {
    Class<?> implementClass = interfaceClass;
    // 接口对应的实现类
    Set<Class<?>> classSetBySuper = ClassHelper.getClassSetBySuper(interfaceClass);
    if (CollectionUtils.isNotEmpty(classSetBySuper)) {
      // 获取第一个实现类
      implementClass = classSetBySuper.iterator().next();
    }
    return implementClass;
  }
}
