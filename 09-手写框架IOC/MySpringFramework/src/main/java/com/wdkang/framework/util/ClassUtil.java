package com.wdkang.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClassUtil {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);

  /**
   * 获取类加载器
   */
  public static ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }
  
  
}
