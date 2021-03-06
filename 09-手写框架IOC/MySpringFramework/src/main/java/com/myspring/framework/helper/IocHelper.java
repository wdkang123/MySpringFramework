package com.myspring.framework.helper;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.myspring.framework.annotation.Autowired;
import com.myspring.framework.util.ReflectionUtil;

/**
 * 依赖注入助手类
 */
public final class IocHelper {

  /**
   * 遍历bean容器所有bean的属性, 为所有带@Autowired注解的属性注入实例
   */
  static {
    // 遍历bean容器里的所有bean
    Logger LOGGER = LoggerFactory.getLogger(IocHelper.class);
    Map<Class<?>, Object> beanMap = BeanHelper.getBeanMap();
    LOGGER.debug("BeanMap => " + beanMap.toString());
    if (MapUtils.isNotEmpty(beanMap)) {
      for (Map.Entry<Class<?>, Object> beanEntry : beanMap.entrySet()) {
        // bean的class类
        Class<?> beanClass = beanEntry.getKey();
        // bean的实例
        Object beanInstance = beanEntry.getValue();
        // 暴力反射获取属性
        Field[] beanFields = beanClass.getDeclaredFields();
        // 遍历bean的属性
        if (ArrayUtils.isNotEmpty(beanFields)) {
          for (Field beanField : beanFields) {
            // 判断属性是否带Autowired注解
            if (beanField.isAnnotationPresent(Autowired.class)) {
              LOGGER.debug("Autowired.class => " + beanField.toString());
              // 属性类型
              Class<?> beanFieldClass = beanField.getType();
              LOGGER.debug("beanField.getType => " + beanField.getType().toString());
              // 如果beanFieldClass是接口, 就获取接口对应的实现类
              beanFieldClass = findImplementClass(beanFieldClass);
              // 获取Class类对应的实例
              Object beanFieldInstance = beanMap.get(beanFieldClass);
              LOGGER.debug("beanFieldInstance => " + beanMap.get(beanFieldClass));
              if (beanFieldInstance != null) {
                ReflectionUtil.setField(beanInstance, beanField, beanFieldInstance);
                LOGGER.debug("ReflectionUtil => " + beanFieldInstance.toString());
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
    // 接口对应的所有实现类
    Set<Class<?>> classSetBySuper = ClassHelper.getClassSetBySuper(interfaceClass);
    if (CollectionUtils.isNotEmpty(classSetBySuper)) {
      // 获取第一个实现类
      implementClass = classSetBySuper.iterator().next();
    }
    return implementClass;
  }
}
