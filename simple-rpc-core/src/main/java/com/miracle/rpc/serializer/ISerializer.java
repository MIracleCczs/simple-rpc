package com.miracle.rpc.serializer;

/**
 * 序列化与反序列化通用接口
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/23 19:22
 */
public interface ISerializer {

    /**
     * 序列化
     * @param obj 目标对象
     * @return
     */
    <T> byte[] serialize(T obj);

    /**
     * 反序列化
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] data, Class<T> clazz);
}
