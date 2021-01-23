package com.mytest.aspect;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.myspring.framework.annotation.Aspect;
import com.myspring.framework.proxy.AspectProxy;

/**
 * 创建切面
 * 在UserServiceImpl中加入了Thread.sleep(1000L); 模拟网络时延
 * 这个切面将在控制台打印信息
 * ===========before begin=========== 
 * time: 1022ms 
 * ===========after end=========== 
 * 
 */
@Aspect(pkg = "com.mytest.controller", cls = "UserController")
public class EfficientAspect extends AspectProxy {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(EfficientAspect.class);
  
  private long begin;
  
  /**
   * 切入点判断
   */
  @Override
  public boolean intercept(Method method, Object[] params) throws Throwable {
    return method.getName().equals("getUserList");
  }
  
  @Override
  public void before(Method method, Object[] params) throws Throwable {
    LOGGER.debug("===========before begin===========");
    begin = System.currentTimeMillis();
  }
  
  @Override
  public void after(Method method, Object[] params) throws Throwable {
    LOGGER.debug(String.format("time: %dms", System.currentTimeMillis() - begin));
    LOGGER.debug("===========after end===========");
  }
  
}
