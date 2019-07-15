package com.rainple.framework.service.impl;

import com.rainple.framework.annotation.Autowired;
import com.rainple.framework.annotation.Service;
import com.rainple.framework.bean.User;
import com.rainple.framework.dao.UserDao;
import com.rainple.framework.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public User add(String name, Integer age) {
        return userDao.add(name,age);
    }

    @Override
    public String get(String name) {
        return name;
    }


}
