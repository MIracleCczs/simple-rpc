package com.miracle.server.service.impl;

import com.miracle.rpc.annotation.SRpcService;
import org.springframework.stereotype.Service;
import pojo.User;
import service.UserService;

/**
 * @author miracle
 * @date 2021/4/19 19:05
 */
@Service
@SRpcService(serviceItf = UserService.class, workThreads = 8)
public class UserServiceImpl implements UserService {

    @Override
    public User saveUser(User user) {
        return user;
    }

    @Override
    public User getUser(String username) {
        return new User(username, 20, "男");
    }

}