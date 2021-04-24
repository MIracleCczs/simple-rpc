package com.miracle.rpc.test.service.impl;

import com.miracle.rpc.test.model.Address;
import com.miracle.rpc.test.model.User;
import com.miracle.rpc.test.service.UserService;

/**
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 10:56
 */
public class UserServiceImpl implements UserService {

    @Override
    public User getUser(String username, String password) {
        Address address = Address.builder().addressCode("010").addressName("安徽").build();
        return User.builder().username(username).password(password).age(12).address(address).build();
    }
}
