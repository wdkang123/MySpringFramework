package com.myspring.framework.helper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.myspring.framework.annotation.RequestMapping;
import com.myspring.framework.bean.Handler;
import com.myspring.framework.bean.Request;

public final class ControllerHelper {
  
  /**
   * REQUEST_MAP 为 请求-处理器 映射
   */
  private static final Map<Request, Handler> REQUEST_MAP = new HashMap<>();
  
  static {
    // 遍历所有Controller类
    Set<Class<?>> controllerClassSet = ClassHelper.getBeanClassSet();
    if (CollectionUtils.isNotEmpty(controllerClassSet)) {
      for (Class<?> controllerClass : controllerClassSet) {
        // 暴力反射获取所以方法
        Method[] methods = controllerClass.getDeclaredMethods();
        // 遍历方法
        if (ArrayUtils.isNotEmpty(methods)) {
          for (Method method : methods) {
            // 判断是否带有RequestMapping注解
            if (method.isAnnotationPresent(RequestMapping.class)) {
              RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
              // 请求路径
              String requestPath = requestMapping.value();
              // 请求方法
              String requestMethod = requestMapping.method().name();
              // 封装请求和处理器
              Request request = new Request(requestMethod, requestPath);
              Handler handler = new Handler(controllerClass, method);
              REQUEST_MAP.put(request, handler);
            }
          }
        }
      }
    }
  }
  
  /**
   * 获取Handler
   */
  public static Handler getHandler(String requestMethod, String requestPath) {
    Request request = new Request(requestMethod, requestPath);
    return REQUEST_MAP.get(request);
  }
}
