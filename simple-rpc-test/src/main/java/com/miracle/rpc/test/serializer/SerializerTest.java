package com.miracle.rpc.test.serializer;

import com.alibaba.fastjson.JSON;
import com.miracle.rpc.serializer.SerializerEngine;
import com.miracle.rpc.test.model.Address;
import com.miracle.rpc.test.model.User;

/**
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/23 19:58
 */
public class SerializerTest {

    public static void main(String[] args) {

        Address address = Address.builder().addressCode("010").addressName("安徽").build();
        User user = User.builder().address(address).age(20).username("miracle").password("miracle").build();

        User defaultSerializer = SerializerEngine.deserialize(SerializerEngine.serialize(user, "1"), User.class, "1");
        User fastjsonSerializer = SerializerEngine.deserialize(SerializerEngine.serialize(user, "2"), User.class, "2");
        User hessianSerializer = SerializerEngine.deserialize(SerializerEngine.serialize(user, "3"), User.class, "3");


/*
        User protobufSerializer = SerializerEngine.deserialize(SerializerEngine.serialize(user, "5"), User.class, "5");
*/
        System.out.println(defaultSerializer);
        System.out.println(fastjsonSerializer);
        System.out.println(hessianSerializer);
/*
        System.out.println(protobufSerializer);
*/

/*        FastJsonSerializer fastJsonSerializer = new FastJsonSerializer();
        byte[] serialize = fastJsonSerializer.serialize(user);
        User deserialize = fastJsonSerializer.deserialize(serialize, User.class);
        System.out.println(deserialize);*/

    }
}
