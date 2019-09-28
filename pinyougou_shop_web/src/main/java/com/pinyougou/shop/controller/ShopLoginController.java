package com.pinyougou.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
@SuppressWarnings("all")
public class ShopLoginController {

    @RequestMapping("/name")
    public Map showName() {
        //1.从spring_secruity中得到用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        //2.将得到的用户名放入到map集合中
        Map map = new HashMap<>();
        map.put("loginName", name);
        return map;
    }
}
