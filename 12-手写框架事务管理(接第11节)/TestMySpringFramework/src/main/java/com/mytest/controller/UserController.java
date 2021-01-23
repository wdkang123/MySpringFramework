package com.mytest.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.myspring.framework.annotation.Autowired;
import com.myspring.framework.annotation.Controller;
import com.myspring.framework.annotation.RequestMapping;
import com.myspring.framework.annotation.RequestMethod;
import com.myspring.framework.bean.Data;
import com.myspring.framework.bean.Param;
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
    List<User> userList = userServiceImpl.getAllUser();
    return new View("index.jsp").addModel("userList", userList);
  }
  
  @RequestMapping(value = "/userInfo", method=RequestMethod.GET)
  public Data getUserInfo(Param param) {
    String id = (String) param.getParamMap().get("id");
    User user = userServiceImpl.getUserInfoById(Integer.parseInt(id));
    return new Data(user);
  }
  
  @RequestMapping(value = "/userEdit", method=RequestMethod.GET)
  public Data editUser(Param param) {
    String id = (String) param.getParamMap().get("id");
    Map<String, Object> fieldMap = new HashMap<>();
    fieldMap.put("password", "999999");
    userServiceImpl.updateUser(Integer.parseInt(id), fieldMap);
    return new Data("success!");
  }
}
