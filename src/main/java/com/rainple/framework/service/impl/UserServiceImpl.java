package com.rainple.framework.service.impl;

import com.rainple.framework.annotation.Service;
import com.rainple.framework.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public String add(String name, Integer age) {
        return "name:" + name + ",age:" + age;
    }

    @Override
    public String get(String name) {
        return name;
    }


}
