package com.miracle.rpc.transport;

import com.miracle.rpc.exception.SRpcException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Netty 请求发起调用线程
 * @author miracle
 * @date 2021/4/16 22:51
 */
@Slf4j
public class RevokerServiceCallable implements Callable<Response> {

    private Channel channel;

    private final InetSocketAddress inetSocketAddress;

    private final Request request;

    public static RevokerServiceCallable of(InetSocketAddress inetSocketAddress, Request request) {
        return new RevokerServiceCallable(inetSocketAddress, request);
    }

    public RevokerServiceCallable(InetSocketAddress inetSocketAddress, Request request) {
        this.inetSocketAddress = inetSocketAddress;
        this.request = request;
    }

    @Override
    public Response call() {
        // 初始化返回结果容器，将本次调用的唯一标识作为Key存入返回结果的Map
        RevokerResponseHolder.initResponseData(request.getUniqueKey());

        // 根据本地调用服务提供者地址获取对应的Netty通道channel队列
        ArrayBlockingQueue<Channel> blockingQueue = NettyChannelPoolFactory.getInstance().acquire(inetSocketAddress);
        try {
            if (channel == null) {
                // 从队列中获取本次调用的Netty通道channel
                channel = blockingQueue.poll(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
            }
            if (channel == null) {
                log.error("can't find channel to resolve this request");
                throw new SRpcException("can't find channel to resolve this request");
            } else {
                while (!channel.isOpen() || !channel.isActive() || !channel.isWritable()) {
                    log.warn("retry get new channel");
                    channel = blockingQueue.poll(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
                    if (channel == null) {
                        // 若队列中没有可用的Channel，则重新注册一个Channel
                        channel = NettyChannelPoolFactory.getInstance().registerChannel(inetSocketAddress);
                    }
                }
                // 将本次调用的信息写入Netty通道，发起异步调用
                ChannelFuture channelFuture = channel.writeAndFlush(request);
                channelFuture.syncUninterruptibly();
                // 从返回结果容器中获取返回结果，同时设置等待超时时间为invokeTimeout
                long invokeTimeout = request.getInvokeTimeout();
                return RevokerResponseHolder.getValue(request.getUniqueKey(), invokeTimeout);
            }
        } catch (Exception e) {
            log.error("service invoke error", e);
            throw new SRpcException("service invoke error", e);
        } finally {
            // 本次调用完毕后，将Netty的通道channel重新释放到队列中，以便下次调用复用
            NettyChannelPoolFactory.getInstance().release(blockingQueue, channel, inetSocketAddress);
        }
    }
}