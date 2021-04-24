package com.miracle.rpc.transport;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.miracle.rpc.exception.SRpcException;
import com.miracle.rpc.loadbalance.LoadBalanceStrategy;
import com.miracle.rpc.loadbalance.LoadBalanceStrategyEngine;
import com.miracle.rpc.model.Provider;
import com.miracle.rpc.register.IRegisterCenter;
import com.miracle.rpc.register.RegisterCenterImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * 服务调用方动态代理实现
 *
 * @author miracle
 * @date 2021/4/19 10:24
 */
@Slf4j
@Data
public class RevokerProxyBeanFactory implements InvocationHandler {

    private ExecutorService executorService;

    private Class<?> targetItf;

    private Long timeout;

    private int consumeThreads;

    private String loadBalanceStrategy;

    public RevokerProxyBeanFactory(Class<?> targetItf, long timeout, int consumeThreads, String loadBalanceStrategy) {
        this.executorService = new ThreadPoolExecutor(consumeThreads, consumeThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new ThreadFactoryBuilder()
                .setNameFormat("simple-rpc-%d").build(), new ThreadPoolExecutor.AbortPolicy());
        this.targetItf = targetItf;
        this.timeout = timeout;
        this.consumeThreads = consumeThreads;
        this.loadBalanceStrategy = loadBalanceStrategy;
    }

    private static volatile RevokerProxyBeanFactory instance;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {

        // 服务接口名称
        String serviceItfName = targetItf.getName();
        IRegisterCenter registerCenter = RegisterCenterImpl.getInstance();
        // 获取服务列表
        List<Provider> providerList = registerCenter.getServiceMetadata().get(serviceItfName);

        if (CollectionUtils.isEmpty(providerList)) {
            log.debug("can't find provider service={}, maybe need to reconnect zk server", serviceItfName);
        } else {
            // 根据负载均衡策略获取对应的服务提供者
            LoadBalanceStrategy loadBalance = LoadBalanceStrategyEngine.getLoadBalanceStrategy(this.loadBalanceStrategy);
            Provider provider = loadBalance.select(providerList);

            Provider providerCopy = Provider.copy(provider);
            providerCopy.setServiceMethod(method);
            providerCopy.setServiceItf(targetItf);

            Request request = Request.builder().args(args)
                    .invokeMethodName(method.getName())
                    .invokeTimeout(timeout)
                    .provider(providerCopy)
                    .uniqueKey(UUID.randomUUID().toString() + "-" + Thread.currentThread().getId())
                    .build();
            try {
                InetSocketAddress socketAddress = new InetSocketAddress(providerCopy.getServerIp(), providerCopy.getServerPort());

                // 发起异步调用请求
                Future<Response> responseFuture = executorService.submit(RevokerServiceCallable.of(socketAddress, request));
                Response response = responseFuture.get(timeout, TimeUnit.MILLISECONDS);
                if (response != null) {
                    return response.getResult();
                }
            } catch (Exception e) {
                log.error("RevokerProxyBeanFactory invoke error, request={}", request, e);
                throw new SRpcException("RevokerProxyBeanFactory invoke error", e);
            }
        }
        return null;
    }

    /**
     * 获取代理对象
     * @return
     */
    public Object getProxy() {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{targetItf}, this);
    }

    public static RevokerProxyBeanFactory getInstance(Class<?> targetItf, long timeout, int consumeThreads, String loadBalanceStrategy) {
        if (null == instance) {
            synchronized (RevokerProxyBeanFactory.class) {
                if (null == instance) {
                    instance = new RevokerProxyBeanFactory(targetItf, timeout, consumeThreads, loadBalanceStrategy);
                }
            }
        }
        return instance;
    }
}