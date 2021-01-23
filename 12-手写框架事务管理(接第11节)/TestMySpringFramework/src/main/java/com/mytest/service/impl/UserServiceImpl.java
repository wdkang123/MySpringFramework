package com.mytest.service.impl;

import java.util.List;
import java.util.Map;

import com.myspring.framework.annotation.Service;
import com.myspring.framework.annotation.Transactional;
import com.myspring.framework.helper.DatabaseHelper;
import com.mytest.pojo.User;
import com.mytest.service.UserService;

@Service
public class UserServiceImpl implements UserService {

  @Override
  public List<User> getAllUser() {
    String sql = "SELECT * FROM user";
    try {
       Thread.sleep(1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return DatabaseHelper.queryEntityList(User.class, sql);
  }

  @Override
  public User getUserInfoById(Integer id) {
    String sql = "SELECT * FROM user WHERE id = ?";
    return DatabaseHelper.queryEntity(User.class, sql, id);
  }

    
  // 这里有注解 @Transactional
  @Transactional
  @Override
  public boolean updateUser(int id, Map<String, Object> fieldMap) {
    return DatabaseHelper.updateEntity(User.class, id, fieldMap);
  }
  
}
