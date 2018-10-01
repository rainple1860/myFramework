package com.rainple.framework.jdbc;

import com.rainple.framework.annotation.Bean;
import com.rainple.framework.annotation.Value;

/**
 * @program: MYSpringFramework
 * @description:
 * @author: Mr.rainple
 * @create: 2018-10-01 10:55
 **/
@Bean
public class DataSource {

    @Value("jdbc.driver")
    private String driver;
    @Value("jdbc.url")
    private String url;
    @Value("jdbc.user")
    private String userName;
    @Value("jdbc.password")
    private String password;

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
