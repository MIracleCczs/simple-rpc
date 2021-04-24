package com.miracle.rpc.transport;

import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.miracle.rpc.exception.SRpcException;
import com.miracle.rpc.model.Provider;
import com.miracle.rpc.register.RegisterCenterImpl;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 服务端业务逻辑处理器
 *
 * @author miracle
 * @date 2021/4/16 16:54
 */
@ChannelHandler.Sharable
@Slf4j
public class NettyServerBizHandler extends SimpleChannelInboundHandler<Request> {

    /**
     * key:serviceItfName:服务接口名
     * value:Semaphore
     * 服务端限流器
     */
    private static final Map<String, Semaphore> SERVICE_KEY_SEMAPHORE_MAP = Maps.newConcurrentMap();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) {
        if (ctx.channel().isWritable()) {
            Provider provider = this.getLocalCacheWithReq(request);
            if (provider == null) {
                log.error("can't find provider for request, request={}", request);
                throw new SRpcException("illegal request, can't find provider for request, request={}" + request);
            } else {
                // 服务提供者信息
                Provider requestProvider = request.getProvider();
                Semaphore semaphore = this.initSemaphore(requestProvider.getServiceItf().getName(), requestProvider.getWorkerThreads());

                Object result = this.invokeMethod(provider, request, semaphore);

                Response response = Response.builder().invokeTimeout(request.getInvokeTimeout())
                        .result(result)
                        .uniqueKey(request.getUniqueKey())
                        .build();
                ctx.writeAndFlush(response);
            }
        } else {
            log.error("channel closed!");
        }
    }

    /**
     * 初始化流控基础设施
     *
     * @param serviceItfName
     * @param workerThreads
     * @return
     */
    private Semaphore initSemaphore(String serviceItfName, int workerThreads) {
        Semaphore semaphore = SERVICE_KEY_SEMAPHORE_MAP.get(serviceItfName);
        if (semaphore == null) {
            synchronized (SERVICE_KEY_SEMAPHORE_MAP) {
                semaphore = SERVICE_KEY_SEMAPHORE_MAP.get(serviceItfName);
                if (semaphore == null) {
                    semaphore = new Semaphore(workerThreads);
                    SERVICE_KEY_SEMAPHORE_MAP.put(serviceItfName, semaphore);
                }
            }
        }
        return semaphore;
    }

    /**
     * 根据请求的方法名称匹配本地服务
     *
     * @param request
     * @return
     */
    private Provider getLocalCacheWithReq(Request request) {
        String invokeMethodName = request.getInvokeMethodName();
        String serviceItfName = request.getProvider().getServiceItf().getName();

        List<Provider> providerList = RegisterCenterImpl.getInstance().getProvidersMap().get(serviceItfName);
        return Collections2.filter(providerList, p -> {
            assert p != null;
            Method serviceMethod = p.getServiceMethod();
            if (serviceMethod != null) {
                return StringUtils.equals(serviceMethod.getName(), invokeMethodName);
            }
            return false;
        }).iterator().next();
    }

    /**
     * 反射调用
     *
     * @param provider  服务提供者
     * @param request   请求信息
     * @param semaphore
     * @return
     */
    private Object invokeMethod(Provider provider, Request request, Semaphore semaphore) {
        Object serviceObject = provider.getServiceObject();
        Method serviceMethod = provider.getServiceMethod();
        Object result = null;
        boolean acquire = false;
        try {
            acquire = semaphore.tryAcquire(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
            if (acquire) {
                result = serviceMethod.invoke(serviceObject, request.getArgs());
            }
        } catch (Exception e) {
            result = e;
            log.error("NettyServerBizHandler invokeMethod error, provider={}, request={}", provider, request, e);
        } finally {
            if (acquire) {
                semaphore.release();
            }
        }
        return result;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("NettyServerBizHandler error, now ctx closed", cause);
        // 发生异常，关闭链路
        ctx.close();
    }
}