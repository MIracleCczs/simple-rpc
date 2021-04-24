package com.miracle.rpc.test.spring;

import com.miracle.rpc.test.model.User;
import com.miracle.rpc.test.service.UserService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 11:00
 */
public class ReferenceTest {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("client-config.xml");

        UserService userService = (UserService) classPathXmlApplicationContext.getBean("remoteUserService");

        User user = userService.getUser("miracle", "miracle");

        System.out.println(user);

    }
}
