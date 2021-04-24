package com.miracle.rpc.test.service;

import com.miracle.rpc.test.model.User;

/**
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 10:55
 */
public interface UserService {

    User getUser(String username, String password);
}
