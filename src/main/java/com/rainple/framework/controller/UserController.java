package com.rainple.framework.controller;


import com.rainple.framework.annotation.*;
import com.rainple.framework.bean.User;
import com.rainple.framework.core.BeanFactory;
import com.rainple.framework.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/rainple/")
public class UserController {

    @Autowired("")
    private UserService userService;

    @RequestMapping("/add")
    @ResponseBody
    public User add( String name, Integer age){
        return userService.add(name,age);
    }

    @ResponseBody
    @RequestMapping("/user/get")
    public User add1(String name) {
        User user = new User();
        user.setName(name);
        return user;
    }

    @RequestMapping("/addMap")
    @ResponseBody
    public Map addMap(Map<String,String> map){
        return map;
    }
    @RequestMapping("/addList")
    @ResponseBody
    public List addList(List<String> list){
        return list;
    }
    @RequestMapping("/addSet")
    @ResponseBody
    public Set addSet(Set<Object> set){
        return set;
    }
    @RequestMapping("{id}/get")
    @ResponseBody
    public User getUser(@PathVariable("id") String id){
        BeanFactory beanFactory = BeanFactory.getBeanFactory();
        User user = null;
        if ("0".equals(id)) {
            user = (User) beanFactory.getBean("user");
        }else if ("1".equals(id)) {
            user = (User) beanFactory.getBean("user1");
        }else {
            user = (User) beanFactory.getBean("user2");
        }
        return user;
    }

    @RequestMapping("/get/{name}/{age}")
    @ResponseBody
    public User get1(@PathVariable("name")String name,@PathVariable("age") Integer age){
        return userService.add(name, age);
    }

    @RequestMapping("/get")
    @ResponseBody
    public String get2(String name){
        return userService.get(name);
    }

    @RequestMapping("hello")
    public String toHello(){
        return "hello";
    }

}
