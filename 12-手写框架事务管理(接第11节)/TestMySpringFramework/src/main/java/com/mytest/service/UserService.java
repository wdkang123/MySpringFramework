package com.mytest.service;

import java.util.List;
import java.util.Map;

import com.mytest.pojo.User;

public interface UserService {
  List<User> getAllUser();
  User getUserInfoById(Integer id);
  boolean updateUser(int id, Map<String, Object> fieldMap);
}
