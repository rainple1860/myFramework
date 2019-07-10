package com.rainple.framework.service.aa;

import com.rainple.framework.annotation.Service;
import com.rainple.framework.bean.User;
import com.rainple.framework.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public User add(String name, Integer age) {
        User user = new User();
        user.setAge(age);
        user.setName(name);
        return user;
    }

    @Override
    public String get(String name) {
        return name;
    }


}
