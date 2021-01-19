package com.wdkang.framework.helper;

import java.util.Properties;

import com.wdkang.framework.ConfigConstant;
import com.wdkang.framework.util.PropsUtil;

public final class ConfigHelper {
  
  /**
   * 加载配置文件属性
   */
  private static final Properties CONFIG_PROPS = PropsUtil.loadProps(ConfigConstant.CONFIG_FILE);

  /**
   * 获得 JDBC 驱动
   */
  public static String getJdbcDriver() {
    return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.JDBC_DRIVER);
  }
  
  /**
   * 获得 JDBC url 
   */
  public static String getJdbcUrl() {
    return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.JDBC_URL);
  }
  
  /**
   * 获得 JDBC 用户名
   */
  public static String getJdbcUsername() {
    return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.JDBC_USERNAME);
  }
  
  /**
   * 获得 JDBC 密码
   */
  public static String getJdbcPassword() {
    return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.JDBC_PASSWORD);
  }
  
  /**
   * 获取应用基础包名
   */
  public static String getAppBasePackage() {
    return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.APP_BASE_PACKAGE);
  }
  
  /**
   * 获取 JSP 路径
   */
  public static String getAppJspPath() {
    return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.APP_JSP_PATH);
  }
  
  /**
   * 获取应用静态资源路径
   */
  public static String getAppAssetPath() {
    return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.APP_ASSET_PATH);
  }
  
  /**
   * 根据属性名字获取String类型的属性值
   */
  public static String getString (String key) {
    return PropsUtil.getString(CONFIG_PROPS, key);
  }
  
  /**
   * 根据属性名字获取int类型的属性值
   */
  public static int getInt(String key) {
    return PropsUtil.getInt(CONFIG_PROPS, key);
  }
  
  /**
   * 根据属性名字获取boolean类型的属性值
   */
  public static boolean getBoolean(String key) {
    return PropsUtil.getBoolean(CONFIG_PROPS, key);
  }
  
}
