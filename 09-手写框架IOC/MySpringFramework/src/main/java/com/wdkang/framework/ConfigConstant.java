package com.wdkang.framework;

public interface ConfigConstant {
  // 配置文件的名称
  String CONFIG_FILE = "handwritten.properties";
  
  // 数据源
  String JDBC_DRIVER = "handwritten.framework.jdbc.driver";
  String JDBC_URl = "handwritten.framework.jdbc.url";
  String JDBC_USERNAME = "handwritten.framework.jdbc.username";
  String JDBC_PASSWORD = "handwritten.framework.jdbc.password";
  
  // java 源码路径
  String APP_BASE_PACKAGE = "handwritten.framework.app.base_package";
  
  // jsp 页面路径
  String APP_JSP_PATH = "handwritten.framework.app.jsp_path";
  
  // 静态资源路径
  String APP_ASSET_PATH = "handwritten.framework.app.asset_path";
}
