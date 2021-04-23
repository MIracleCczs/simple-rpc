package com.miracle.rpc.serializer;

import com.miracle.rpc.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/23 19:27
 */
@Slf4j
public class JacksonSerializer implements ISerializer{

    @Override
    public <T> byte[] serialize(T obj) {
        if (obj  == null) {
            throw new NullPointerException("JacksonSerializer serialize data is null");
        }
        return JacksonUtil.objectToJson(obj).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data  == null) {
            throw new NullPointerException("JacksonSerializer deserialize data is null");
        }
        String json = new String(data);
        return JacksonUtil.jsonToObject(json, clazz);
    }
}
