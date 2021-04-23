package com.miracle.rpc.test.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/23 19:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements Serializable {


    private String username;

    private int age;

    private String password;

    private Address address;

}
