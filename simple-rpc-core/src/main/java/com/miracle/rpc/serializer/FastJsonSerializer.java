package com.miracle.rpc.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 基于fastjson的序列化与反序列化
 *
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/23 19:34
 */
public class FastJsonSerializer implements ISerializer {

    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) {
            throw new NullPointerException("FastJsonSerializer serialize data is null");
        }
        JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        return JSON.toJSONString(obj, SerializerFeature.WriteDateUseDateFormat).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data == null) {
            throw new NullPointerException("FastJsonSerializer deserialize data is null");
        }
        return JSON.parseObject(new String(data), clazz);
    }
}
