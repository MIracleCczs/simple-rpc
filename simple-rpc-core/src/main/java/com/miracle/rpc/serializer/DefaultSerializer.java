package com.miracle.rpc.serializer;

import com.miracle.rpc.exception.SRpcException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * java默认实现
 * 优点： java语言自带，无须引入第三方依赖
 * <p>
 * 缺点： 只支持java语言，不支持跨语言
 * 性能欠佳，并且码流过大
 *
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/23 19:23
 */
@Slf4j
public class DefaultSerializer implements ISerializer{

    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) {
            throw new NullPointerException("DefaultSerializer serialize data is null");
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.close();
        } catch (IOException e) {
            log.error("DefaultSerializer serialize error, obj={}", obj, e);
            throw new SRpcException("DefaultSerializer serialize error", e);
        }

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data == null) {
            throw new NullPointerException("DefaultSerializer deserialize data is null");
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);

        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (T) objectInputStream.readObject();
        } catch (Exception e) {
            log.error("DefaultSerializer deserialize error, data={}", data, e);
            throw new SRpcException("DefaultSerializer deserialize error", e);
        }
    }
}
