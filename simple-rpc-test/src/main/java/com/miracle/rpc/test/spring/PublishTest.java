package com.miracle.rpc.test.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/24 10:53
 */
public class PublishTest {


    public static void main(String[] args) {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("server-config.xml");
        
    }
}
