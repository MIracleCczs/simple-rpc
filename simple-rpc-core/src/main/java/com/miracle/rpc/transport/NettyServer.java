package com.miracle.rpc.transport;

import com.miracle.rpc.exception.SRpcException;
import com.miracle.rpc.serializer.SerializerTypeEnum;
import com.miracle.rpc.transport.codec.NettyDecoderHandler;
import com.miracle.rpc.transport.codec.NettyEncoderHandler;
import com.miracle.rpc.util.SimpleRpcPropertiesUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端启动
 *
 * @author miracle
 * @version 1.0.0
 * @date 2021-04-15 19:05
 **/
@Slf4j
public class NettyServer {

    private static final NettyServer NETTY_SERVER = new NettyServer();

    private EventLoopGroup bossGroup;

    private EventLoopGroup workGroup;

    private static final SerializerTypeEnum SERIALIZE_TYPE = SerializerTypeEnum.getByType(SimpleRpcPropertiesUtil.getSerializeType());

    /**
     * 启动服务
     *
     * @param port 监听端口
     */
    public void startServer(int port) {
        synchronized (NettyServer.class) {
            if (bossGroup != null || workGroup != null) {
                log.debug("netty server is already start");
            } else {
                bossGroup = new NioEventLoopGroup(1);
                workGroup = new NioEventLoopGroup();
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                // 解码器
                                socketChannel.pipeline().addLast(new NettyDecoderHandler(Request.class, SERIALIZE_TYPE));
                                // 编码器
                                socketChannel.pipeline().addLast(new NettyEncoderHandler(SERIALIZE_TYPE));
                                // 服务处理
                                socketChannel.pipeline().addLast(new NettyServerBizHandler());
                            }
                        });
                try {
                    bootstrap.bind(port).sync().channel();
                    log.info("NettyServer startServer start now!!!");
                } catch (Exception e) {
                    log.error("NettyServer startServer error", e);
                    throw new SRpcException("NettyServer startServer error", e);
                }
            }
        }
    }

    public static NettyServer getInstance() {
        return NETTY_SERVER;
    }

    private NettyServer() {
    }
}
