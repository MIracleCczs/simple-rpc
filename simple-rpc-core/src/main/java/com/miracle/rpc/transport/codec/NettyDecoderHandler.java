package com.miracle.rpc.transport.codec;

import com.miracle.rpc.serializer.SerializerEngine;
import com.miracle.rpc.serializer.SerializerTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 解码器
 * @author miracle
 * @date 2021/4/16 16:39
 */
public class NettyDecoderHandler extends ByteToMessageDecoder {

    private final Class<?> genericClass;
    private final SerializerTypeEnum serializeType;

    public NettyDecoderHandler(Class<?> genericClass, SerializerTypeEnum serializeType) {
        this.genericClass = genericClass;
        this.serializeType = serializeType;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 获取消息头所标识的消息体字节数组长度
        int readableLength = in.readableBytes();
        if (readableLength < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        // 如果当前可读长度小于目标长度，则返回知道可以获取到的字节数组长度等于目标长度
        if (readableLength < dataLength) {
            in.resetReaderIndex();
            return;
        }
        // 读取完整的消息体字节数组
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        // 将字节数组反序列化为java对象
        Object obj = SerializerEngine.deserialize(data, genericClass, serializeType.getTypeCode());
        out.add(obj);
    }
}