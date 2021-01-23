package com.myspring.framework.bean;

import java.lang.reflect.Method;

public class Handler {
  /**
   * Controller ¿‡
   */
  private Class<?> controllerClass;
  
  /**
   * Controller ∑Ω∑®
   */
  private Method controllerMethod;
  
  public Handler(Class<?> controllerClass, Method controllerMethod) {
    this.controllerClass = controllerClass;
    this.controllerMethod = controllerMethod;
  }
  
  public Class<?> getControllerClass() {
    return controllerClass;
  }
  
  public Method getControllerMethod() {
    return controllerMethod;
  }
  
}
