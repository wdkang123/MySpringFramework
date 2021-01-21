package com.mytest.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.myspring.framework.annotation.Autowired;
import com.myspring.framework.annotation.Controller;
import com.mytest.pojo.User;
import com.mytest.service.UserService;
import com.mytest.service.impl.UserServiceImpl;


@WebServlet("/test")
@Controller
public class MySpringServlet extends HttpServlet {
  
  @Autowired
  private UserService userServiceImpl;
  
  private static final long serialVersionUID = 1L;
  
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println(this.userServiceImpl);
    List<User> list = this.userServiceImpl.getAll();
    resp.getWriter().write(list.toString());
  }
}
