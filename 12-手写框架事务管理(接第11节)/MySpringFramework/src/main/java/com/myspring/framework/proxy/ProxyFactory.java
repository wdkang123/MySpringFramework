package com.myspring.framework.proxy;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ProxyFactory {
  /**
   * 输入一个目标 和 一组Proxy接口实现
   * 输出一个代理对象
   * @return 
   */
  @SuppressWarnings("unchecked")
  public static <T> T createProxy(final Class<?> targetClass, final List<Proxy> proxyList) {
    return (T) Enhancer.create(targetClass, new MethodInterceptor() {
      /**
       * 代理方法
       */
      @Override
      public Object intercept(Object targetObject, Method targetMethod, Object[] methodParams, MethodProxy methodProxy) throws Throwable {
        return new ProxyChain(targetClass, targetObject, targetMethod, methodProxy, methodParams, proxyList).doProxyChain();
      }
    });
  }
  
}
