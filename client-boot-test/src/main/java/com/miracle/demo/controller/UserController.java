package com.miracle.demo.controller;

import com.miracle.rpc.annotation.SRpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojo.User;
import service.UserService;

/**
 * @author miracle
 * @date 2021/4/19 19:46
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @SRpcReference(targetItf = UserService.class, remoteAppKey = "test")
    private UserService userService;

    @GetMapping("/getUser/{username}")
    public User getUser(@PathVariable String username) {

        return userService.getUser(username);
    }
}