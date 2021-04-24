package service;

import pojo.User;

/**
 * @author miracle
 * @version 1.0.0
 * @date 2021-04-15 20:28
 **/
public interface UserService {


    User saveUser(User user);

    User getUser(String username);

}
