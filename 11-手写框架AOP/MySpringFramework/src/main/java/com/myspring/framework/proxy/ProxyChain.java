package com.myspring.framework.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.proxy.MethodProxy;

public class ProxyChain {
  
  // 目标类
  private final Class<?> targetClass;
  // 目标对象
  private final Object targetObject;
  // 目标方法
  private final Method targetMethod;
  // 方法代理
  private final MethodProxy methodProxy;
  // 方法参数
  private final Object[] methodParams;
  
  // 代理列表
  private List<Proxy> proxyList = new ArrayList<>();
  // 代理索引
  private int proxyIndex = 0;
  
  public ProxyChain(Class<?> targetClass, Object targetObject, Method targetMethod, MethodProxy methodProxy, Object[] methodParams, List<Proxy> proxyList) {
    this.targetClass = targetClass;
    this.targetObject = targetObject;
    this.targetMethod = targetMethod;
    this.methodProxy = methodProxy;
    this.methodParams = methodParams;
    this.proxyList = proxyList;
  }
  
  public Object[] getMethodParams() {
    return methodParams;
  }
  
  public Class<?> getTargetClass() {
    return targetClass;
  }
  
  public Method getTargetMethod() {
    return targetMethod;
  }
  
  /**
   * 递归执行
   */
  public Object doProxyChain() throws Throwable {
    Object methodResult;
    if (proxyIndex < proxyList.size()) {
      // 执行方法增强
      methodResult = proxyList.get(proxyIndex ++).doProxy(this);
    } else {
      // 目标方法最后执行一次且执行一次
      methodResult = methodProxy.invokeSuper(targetObject, methodParams);
    }
    return methodResult;
  }
  
}
