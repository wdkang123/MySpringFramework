package com.myspring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.myspring.framework.helper.IocHelper;

public class Main {
  
  private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);
  
  public static void main(String[] args) {
    LOGGER.debug("Hello Ioc !");
    new IocHelper();
  }
}
