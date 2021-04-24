package com.miracle.rpc.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端业务处理逻辑Handler
 * @author miracle
 * @date 2021/4/16 22:10
 */
@Slf4j
public class NettyClientBizHandler extends SimpleChannelInboundHandler<Response> {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("NettyClientBizHandler exceptionCaught", cause);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Response response) {
        // 将Netty异步返回的结果存入阻塞队列，以便调用端同步获取
        RevokerResponseHolder.putResultValue(response);
    }
}