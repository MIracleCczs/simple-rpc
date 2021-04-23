package com.miracle.rpc.serializer;

import com.google.protobuf.GeneratedMessageV3;
import com.miracle.rpc.exception.SRpcException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Protobuf: 空间小，高解析性能
 * 但是需要根据IDL生成类
 *
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/23 19:28
 */
@Slf4j
public class ProtoBufSerializer implements ISerializer {

    @Override
    public <T> byte[] serialize(T obj) {
        try {
            if (obj instanceof GeneratedMessageV3) {
                throw new UnsupportedOperationException("ProtoBufSerializer serialize not support obj type");
            }
            return (byte[]) MethodUtils.invokeMethod(obj, "toByteArray");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("ProtoBufSerializer serialize error, obj={}", obj, e);
            throw new SRpcException("ProtoBufSerializer serialize error", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try {
            if (!GeneratedMessageV3.class.isAssignableFrom(clazz)) {
                throw new UnsupportedOperationException("ProtoBufSerializer deserialize not support obj type");
            }
            return (T) MethodUtils.invokeStaticMethod(clazz, "getDefaultInstance");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("ProtoBufSerializer deserialize error", e);
            throw new SRpcException("ProtoBufSerializer deserialize error", e);
        }
    }

}
