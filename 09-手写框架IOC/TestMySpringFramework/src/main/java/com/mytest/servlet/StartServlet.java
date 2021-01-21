package com.mytest.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.myspring.framework.helper.IocHelper;


public class StartServlet extends HttpServlet {
  
  private static final long serialVersionUID = 1L;

  @Override
  public void init() throws ServletException {
    new IocHelper();
  }
}
