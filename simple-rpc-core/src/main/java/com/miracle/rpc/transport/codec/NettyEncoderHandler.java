package com.miracle.rpc.transport.codec;

import com.miracle.rpc.serializer.SerializerEngine;
import com.miracle.rpc.serializer.SerializerTypeEnum;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器
 * @author miracle
 * @date 2021/4/16 16:38
 */
public class NettyEncoderHandler extends MessageToByteEncoder {

    private final SerializerTypeEnum serializeType;

    public NettyEncoderHandler(SerializerTypeEnum serializeType) {
        this.serializeType = serializeType;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf out) {
        // 将对象序列化为字节数组
        byte[] bytes = SerializerEngine.serialize(msg, serializeType.getTypeCode());
        // 将字节数组的长度作为消息头写入，解决半包/粘包问题
        out.writeInt(bytes.length);
        // 写入序列化后得到的字节数组
        out.writeBytes(bytes);
    }
}