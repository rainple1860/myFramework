package com.rainple.framework.service;

import com.rainple.framework.bean.User;

public interface UserService {

    User add(String name, Integer age);

    String get(String name);

}
