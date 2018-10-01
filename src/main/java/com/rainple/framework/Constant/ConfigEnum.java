package com.rainple.framework.Constant;

public enum ConfigEnum {
    DISPATCHER_PREFIX("com.rainple.dispatcher.prefix"),
    DISPATCHER_SUBFIX("com.rainple.dispatcher.subfix"),
    SCANPACKAGE("com.rainple.scanPack"),
    JDBC_DRIVER("jdbc.driver"),
    JDBC_URL("jdbc.url"),
    JDBC_USER("jdbc.user"),
    JDBC_PASSWORD("jdbc.password");
    private String name;
    ConfigEnum(String name){
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
