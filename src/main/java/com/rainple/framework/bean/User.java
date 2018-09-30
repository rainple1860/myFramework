package com.rainple.framework.bean;

import com.rainple.framework.annotation.Bean;
import com.rainple.framework.annotation.Value;

@Bean("user2")
public class User {
    @Value("com.user.name")
    private String name;
    @Value("com.user.age")
    private Integer age;

    public User(){ }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
