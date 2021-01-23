package com.myspring.framework.proxy;

public interface Proxy {
  /**
   * 执行链式代理
   * 所谓链式代理 可将多个代理通过一条链子穿起来 一个个的去执行 
   * 执行顺序取决于加入到链上的先后顺序
   */
  Object doProxy(ProxyChain proxyChain) throws Throwable; 
}
