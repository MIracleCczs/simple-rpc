package com.miracle.rpc.serializer;

import com.google.common.collect.Maps;
import com.miracle.rpc.exception.SRpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 序列化服务引擎
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/23 19:38
 */
@Slf4j
public class SerializerEngine {


    private static final Map<SerializerTypeEnum, ISerializer> SERIALIZER_MAP = Maps.newHashMap();

    static {
        SERIALIZER_MAP.put(SerializerTypeEnum.DEFAULT_SERIALIZER, new DefaultSerializer());
        SERIALIZER_MAP.put(SerializerTypeEnum.JACKSON_SERIALIZER, new JacksonSerializer());
        SERIALIZER_MAP.put(SerializerTypeEnum.FASTJSON_SERIALIZER, new FastJsonSerializer());
        SERIALIZER_MAP.put(SerializerTypeEnum.HESSIAN_SERIALIZER, new HessianSerializer());
        SERIALIZER_MAP.put(SerializerTypeEnum.PROTOBUF_SERIALIZER, new ProtoBufSerializer());
    }

    /**
     * 序列化
     * @param obj 对象
     * @param serializeType 序列化方式
     * @param <T>
     * @return
     */
    public static <T> byte[] serialize(T obj, String serializeType) {
        SerializerTypeEnum serialize = SerializerTypeEnum.getByType(serializeType);
        if (null == serialize) {
            throw new SRpcException("SerializerEngine serialize error, serializeType is not support");
        }
        ISerializer iSerializer = SERIALIZER_MAP.get(serialize);
        if (null == iSerializer) {
            throw new SRpcException("SerializerEngine serialize error, get ISerializer fail");
        }
        try {
            return iSerializer.serialize(obj);
        } catch (Exception e) {
            log.error("SerializerEngine serialize error, obj={}", obj, e);
            throw new SRpcException("SerializerEngine serialize error");
        }
    }

    /**
     * 反序列化
     * @param data 数据
     * @param clazz
     * @param serializeType
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data, Class<T> clazz, String serializeType) {
        SerializerTypeEnum serialize = SerializerTypeEnum.getByType(serializeType);
        if (null == serialize) {
            throw new SRpcException("SerializerEngine deserialize error, serializeType is not support");
        }
        ISerializer iSerializer = SERIALIZER_MAP.get(serialize);
        if (null == iSerializer) {
            throw new SRpcException("SerializerEngine deserialize error, get ISerializer fail");
        }
        try {
            return iSerializer.deserialize(data, clazz);
        } catch (Exception e) {
            log.error("SerializerEngine deserialize error, data={}", data, e);
            throw new SRpcException("SerializerEngine deserialize error");
        }
    }
}
