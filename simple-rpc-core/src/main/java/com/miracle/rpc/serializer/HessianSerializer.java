package com.miracle.rpc.serializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.miracle.rpc.exception.SRpcException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 基于hessian实现的序列化与反序列化
 * 推荐使用：支持跨语言，性能高，码流小
 * @author : miracle
 * @version : 1.0.0
 * @date : 2021/4/23 19:26
 */
@Slf4j
public class HessianSerializer implements ISerializer{

    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) {
            throw new NullPointerException("HessianSerializer serialize obj is null");
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            HessianOutput output = new HessianOutput(byteArrayOutputStream);
            output.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("HessianSerializer serialize error. obj = {}", obj, e);
            throw new SRpcException("HessianSerializer serialize error", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data  == null) {
            throw new NullPointerException("HessianSerializer deserialize data is null");
        }
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            return (T) hessianInput.readObject();
        } catch (IOException e) {
            log.error("HessianSerializer deserialize error. data = {}", data, e);
            throw new SRpcException("HessianSerializer deserialize error", e);
        }
    }
}
