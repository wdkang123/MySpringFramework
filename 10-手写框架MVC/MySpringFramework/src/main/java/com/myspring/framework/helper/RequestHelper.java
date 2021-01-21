package com.myspring.framework.helper;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.myspring.framework.bean.Param;

public final class RequestHelper {
  /**
   * 获取请求参数
   */
  public static Param createParam(HttpServletRequest request) throws IOException {
    Map<String, Object> paramMap = new HashMap<>();
    Enumeration<String> paramNames = request.getParameterNames();
    // 没有参数
    if (!paramNames.hasMoreElements()) {
      return null;
    }
    // get 和 post 参数都能获取到
    while (paramNames.hasMoreElements()) {
      String fieldName = paramNames.nextElement();
      String fieldValue = request.getParameter(fieldName);
      paramMap.put(fieldName, fieldValue);
    }
    return new Param(paramMap);
  }
}
