package com.mytest.controller;

import java.util.List;

import com.myspring.framework.annotation.Autowired;
import com.myspring.framework.annotation.Controller;
import com.myspring.framework.annotation.RequestMapping;
import com.myspring.framework.annotation.RequestMethod;
import com.myspring.framework.bean.View;
import com.mytest.pojo.User;
import com.mytest.service.UserService;

@Controller
public class UserController {
  @Autowired
  private UserService userServiceImpl;
  
  /**
   * 用户列表
   */
  @RequestMapping(value = "/userList", method=RequestMethod.GET)
  public View getUserList() {
    List<User> userList = userServiceImpl.getAll();
    return new View("index.jsp").addModel("userList", userList);
  }
}
