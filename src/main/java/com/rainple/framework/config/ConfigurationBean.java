package com.rainple.framework.config;

import com.rainple.framework.annotation.Bean;
import com.rainple.framework.annotation.Configuration;
import com.rainple.framework.bean.User;

@Configuration
public class ConfigurationBean {

    @Bean
    public User instanceUser(){
        User user = new User();
        user.setAge(30);
        user.setName("老王");
        return user;
    }

    @Bean("user1")
    public User instance1(){
        User user = new User();
        user.setAge(40);
        user.setName("以配置类方式注入");
        return user;
    }

}
