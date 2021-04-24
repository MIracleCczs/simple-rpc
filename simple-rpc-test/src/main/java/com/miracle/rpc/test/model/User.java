package com.miracle.rpc.test.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.*;
import org.apache.commons.lang3.ClassUtils;

import java.io.Serializable;
import java.util.LinkedHashMap;

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

    private Object address;
}
