package com.rainple.framework.dao;

import com.rainple.framework.annotation.Autowired;
import com.rainple.framework.annotation.Repository;
import com.rainple.framework.bean.User;

@Repository
public class UserDao {

    @Autowired
    private StudentDao studentDao;

    public User add(String name,int age) {
        User user = new User();
        user.setName(name);
        user.setAge(age);
        studentDao.add(name);
        return user;
    }

}
