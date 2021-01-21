package com.mytest.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.myspring.framework.annotation.Autowired;
import com.myspring.framework.annotation.Service;
import com.mytest.pojo.User;
import com.mytest.service.UserService;

@Service
public class UserServiceImpl implements UserService {

  @Override
  public List<User> getAll() {
    List<User> list = new ArrayList<>();
    User user1 = new User("zhangsan", "123");
    User user2 = new User("lisi", "456");
    User user3 = new User("wangwu", "789");
    list.add(user1);
    list.add(user2);
    list.add(user3);
    return list;
  }
  
}
