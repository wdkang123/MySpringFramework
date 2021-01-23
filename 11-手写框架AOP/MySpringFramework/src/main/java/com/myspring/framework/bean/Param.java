package com.myspring.framework.bean;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;

public class Param {
  
  private Map<String, Object> paramMap;
  
  public Param() {
    
  }
  
  public Param(Map<String, Object> paramMap) {
    this.paramMap = paramMap;
  }
  
  public Map<String, Object> getParamMap() {
    return paramMap;
  }
  
  public boolean isEmpty() {
    return MapUtils.isEmpty(paramMap);
  }
}
