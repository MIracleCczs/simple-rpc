package com.miracle.rpc.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.miracle.rpc.model.Provider;
import com.miracle.rpc.serializer.SerializerTypeEnum;
import com.miracle.rpc.transport.codec.NettyDecoderHandler;
import com.miracle.rpc.transport.codec.NettyEncoderHandler;
import com.miracle.rpc.util.SimpleRpcPropertiesUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * channel 连接池工厂
 *
 * @author miracle
 * @date 2021/4/16 21:10
 */
@Slf4j
public class NettyChannelPoolFactory {

    private static final NettyChannelPoolFactory CHANNEL_POOL_FACTORY = new NettyChannelPoolFactory();

    /**
     * key 为服务提供者地址，value为Netty Channel阻塞队列
     */
    private static final Map<InetSocketAddress, ArrayBlockingQueue<Channel>> CHANNEL_POOL_MAP = Maps.newConcurrentMap();
    /**
     * 初始化Netty Channel阻塞队列的长度，该值为可配置信息
     */
    private static final int CHANNEL_CONNECT_SIZE = SimpleRpcPropertiesUtil.getChannelConnectSize();

    private static final SerializerTypeEnum SERIALIZE_TYPE = SerializerTypeEnum.getByType(SimpleRpcPropertiesUtil.getSerializeType());

    /**
     * 初始化Netty Channel连接队列
     *
     * @param providerMap
     */
    public void initChannelPoolFactory(Map<String, List<Provider>> providerMap) {
        // 服务提供者信息
        Collection<List<Provider>> metaDataCollection = providerMap.values();

        // 获取服务提供者地址列表
        Set<InetSocketAddress> socketAddressSet = Sets.newHashSet();
        for (List<Provider> serviceMetaDataModels : metaDataCollection) {
            for (Provider provider : serviceMetaDataModels) {
                String serverIp = provider.getServerIp();
                int serverPort = provider.getServerPort();
                InetSocketAddress inetSocketAddress = new InetSocketAddress(serverIp, serverPort);
                socketAddressSet.add(inetSocketAddress);
            }
        }
        // 根据服务提供者地址列表初始化Channel阻塞队列，并以地址为Key，地址
        // 对应的Channel阻塞队列为value，存入channelPoolMap
        for (InetSocketAddress inetSocketAddress : socketAddressSet) {
            try {
                int realChannelConnectSize = 0;
                while (realChannelConnectSize < CHANNEL_CONNECT_SIZE) {
                    Channel channel = null;
                    while (channel == null) {
                        // 若channel不存在，则注册新的Netty Channel
                        channel = registerChannel(inetSocketAddress);
                    }
                    // 计数器，初始化的时候存入阻塞队列的Netty Channel个数不超过CHANNEL_CONNECT_SIZE
                    realChannelConnectSize++;

                    // 将新注册的Netty Channel存入阻塞队列channelArrayBlockingQueue
                    // 并将阻塞队列channelArrayBlockingQueue作为value存入channelPoolMap
                    ArrayBlockingQueue<Channel> channelArrayBlockingQueue = CHANNEL_POOL_MAP.get(inetSocketAddress);
                    if (channelArrayBlockingQueue == null) {
                        channelArrayBlockingQueue = new ArrayBlockingQueue<>(CHANNEL_CONNECT_SIZE);
                        CHANNEL_POOL_MAP.put(inetSocketAddress, channelArrayBlockingQueue);
                    }
                    boolean offer = channelArrayBlockingQueue.offer(channel);
                    if (!offer) {
                        log.debug("channelArrayBlockingQueue fail");
                    }
                }
            } catch (Exception e) {
                log.error("initChannelPoolFactory error", e);
            }
        }
    }

    /**
     * 根据服务提供者地址获取对应的Netty Channel阻塞队列
     *
     * @param socketAddress
     * @return
     */
    public ArrayBlockingQueue<Channel> acquire(InetSocketAddress socketAddress) {
        return CHANNEL_POOL_MAP.get(socketAddress);
    }

    /**
     * Channel使用完毕后，回收到arrayBlockingQueue
     *
     * @param arrayBlockingQueue
     * @param channel
     * @param socketAddress
     */
    public void release(ArrayBlockingQueue<Channel> arrayBlockingQueue, Channel channel, InetSocketAddress socketAddress) {
        if (arrayBlockingQueue == null) {
            return;
        }
        // 回收之前检查Channel是否可用，不可用重新注册一个
        if (channel == null || !channel.isActive() || !channel.isOpen() || !channel.isWritable()) {
            if (channel != null) {
                channel.deregister().syncUninterruptibly().awaitUninterruptibly();
                channel.closeFuture().syncUninterruptibly().awaitUninterruptibly();
            } else {
                Channel newChannel = null;
                while (newChannel == null) {
                    log.debug("register new channel!!");
                    newChannel = registerChannel(socketAddress);
                }
                arrayBlockingQueue.offer(newChannel);
                return;
            }
        }
        arrayBlockingQueue.offer(channel);
    }

    /**
     * 为服务提供者注册新的Channel
     *
     * @param socketAddress 服务端信息
     * @return
     */
    public Channel registerChannel(InetSocketAddress socketAddress) {
        try {
            EventLoopGroup group = new NioEventLoopGroup(10);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.remoteAddress(socketAddress);
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 注册Netty编码器
                            ch.pipeline().addLast(new NettyEncoderHandler(SERIALIZE_TYPE));
                            // 注册Netty解码器
                            ch.pipeline().addLast(new NettyDecoderHandler(Response.class, SERIALIZE_TYPE));
                            // 注册客户端业务处理逻辑Handler
                            ch.pipeline().addLast(new NettyClientBizHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            final Channel channel = channelFuture.channel();

            final CountDownLatch countDownLatch = new CountDownLatch(1);
            final List<Boolean> isSuccessHolder = Lists.newArrayListWithCapacity(1);
            // 监听channel是否建立成功
            channelFuture.addListener(future -> {
                if (future.isSuccess()) {
                    isSuccessHolder.add(Boolean.TRUE);
                } else {
                    // 如果建立失败，保存建立失败标记
                    log.error("registerChannel fail , {}", future.cause().getMessage());
                    isSuccessHolder.add(Boolean.FALSE);
                }
                countDownLatch.countDown();
            });
            countDownLatch.await();
            // 如果Channel建立成功，返回新建的Channel
            if (Boolean.TRUE.equals(isSuccessHolder.get(0))) {
                return channel;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("registerChannel fail", e);
        }
        return null;
    }

    public static NettyChannelPoolFactory getInstance() {
        return CHANNEL_POOL_FACTORY;
    }

    private NettyChannelPoolFactory() {

    }
}